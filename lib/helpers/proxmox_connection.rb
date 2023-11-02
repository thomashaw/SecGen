require 'rest-client'
require 'retryable'
require 'irb'

# We only use some of these functions in SecGen currently
# Partly based on examples from the MIT Licensed Vagrant-Proxmox gem
# Could improve and extract this out to a generalised proxmox gem

module Proxmox
  module ApiError
    class InvalidCredentials < StandardError
    end
    class ConnectionError < StandardError
    end
    class CommunicationError < StandardError
    end
    class NotImplemented < StandardError
    end
    class ServerError < StandardError
    end
    class UnauthorizedError < StandardError
    end
  end
  class ProxmoxTaskNotFinished < StandardError
  end
  class ProxmoxTaskTimeout < StandardError
  end
  class Connection

    attr_reader :api_url
    attr_reader :ticket
    attr_reader :csrf_token
    attr_accessor :vm_id_range
    attr_accessor :task_timeout
    attr_accessor :task_status_check_interval
    attr_accessor :imgcopy_timeout
    attr_accessor :vm_info_cache

    def initialize(api_url, opts = {})
      @api_url = api_url
      @vm_id_range = opts[:vm_id_range] || (900..999)
      @task_timeout = opts[:task_timeout] || 60
      @task_status_check_interval = opts[:task_status_check_interval] || 2
      @imgcopy_timeout = opts[:imgcopy_timeout] || 120
      @vm_info_cache = {}

    end

    def login(username: required('username'), password: required('password'))
      response = post '/access/ticket', username: username, password: password
      @ticket = response[:data][:ticket]
      @csrf_token = response[:data][:CSRFPreventionToken]
    rescue => x
      raise ApiError::ConnectionError, x.message
    end

    def get_vm_state(vm_id)
      vm_info = get_vm_info vm_id
      if vm_info
        begin
          response = get "/nodes/#{vm_info[:node]}/#{vm_info[:type]}/#{vm_id}/status/current"
          states = { 'running' => :running,
                     'stopped' => :stopped }
          states[response[:data][:status]]
        rescue ApiError::ServerError
          :not_created
        end
      else
        :not_created
      end
    end

    def wait_for_completion(task_response: required('task_response'), timeout_message: required('timeout_message'))

      task_upid = task_response[:data]
      timeout = task_timeout
      task_type = /UPID:.*?:.*?:.*?:.*?:(.*)?:.*?:.*?:/.match(task_upid)[1]
      timeout = imgcopy_timeout if task_type == 'imgcopy'
      begin
        retryable(on: Proxmox::ProxmoxTaskNotFinished,
                  tries: timeout / task_status_check_interval + 1,
                  sleep: task_status_check_interval) do
          exit_status = get_task_exitstatus task_upid
          exit_status.nil? ? raise(Proxmox::ProxmoxTaskNotFinished) : exit_status
        end
      rescue Proxmox::ProxmoxTaskNotFinished
        raise Proxmox::ProxmoxTaskTimeout, timeout_message
      end
    end

    def delete_vm(vm_id)
      vm_info = get_vm_info vm_id
      response = delete "/nodes/#{vm_info[:node]}/#{vm_info[:type]}/#{vm_id}"
      wait_for_completion task_response: response, timeout_message: 'vagrant_proxmox.errors.destroy_vm_timeout'
    end

    def qemu_agent_get_ip(vm_id)
      vm_info = get_vm_info vm_id
      begin
        # binding.irb
        response = get "/nodes/#{vm_info[:node]}/#{vm_info[:type]}/#{vm_id}/agent/network-get-interfaces"
      rescue ApiError::ServerError
        return nil
      rescue RestClient::InternalServerError
        return nil
      end
      # select the first nic (0 = lo, 1 = first nic)
      response.dig(:data, :result).each do |nic|
        nic.dig(:"ip-addresses").each do |ip_addresses_block|
          # find an IPv4 address and return it
          if ip_addresses_block.dig(:"ip-address-type") == "ipv4"
            ip = ip_addresses_block.dig(:"ip-address")
            return ip if ip != "127.0.0.1"
          end
        end
      end
      nil
    end

    def clone_vm(node: required('node'), vm_type: required('node'), params: required('params'))
      vm_id = params[:vmid]
      params.delete(:vmid)
      params.delete(:ostype)
      params.delete(:ide2)
      params.delete(:sata0)
      params.delete(:sockets)
      params.delete(:cores)
      params.delete(:description)
      params.delete(:memory)
      params.delete(:net0)
      retries = 5
      begin
        response = post "/nodes/#{node}/#{vm_type}/#{vm_id}/clone", params
        wait_for_completion task_response: response, timeout_message: 'vagrant_proxmox.errors.create_vm_timeout'
      rescue => e
        if retries > 0
          puts "Error: #{e.message}. Retrying..."
          retries -= 1
          sleep 5
          retry
        else
          puts "Failed to clone VM after multiple retries."
          raise e # Raising the error after retries are exhausted
        end
      end
    end

    def config_clone(node: required('node'), vm_type: required('node'), params: required('params'))
      vm_id = params[:vmid]
      params.delete(:vmid)
      response = post "/nodes/#{node}/#{vm_type}/#{vm_id}/config", params
      wait_for_completion task_response: response, timeout_message: 'vagrant_proxmox.errors.create_vm_timeout'
    end

    def get_vm_config(node: required('node'), vm_id: required('node'), vm_type: required('node'))
      response = get "/nodes/#{node}/#{vm_type}/#{vm_id}/config"
      response = response[:data]
      response.empty? ? raise(Proxmox::Errors::VMConfigError) : response
    end

    def start_vm(vm_id)
      vm_info = get_vm_info vm_id
      response = post "/nodes/#{vm_info[:node]}/#{vm_info[:type]}/#{vm_id}/status/start", nil
      wait_for_completion task_response: response, timeout_message: 'vagrant_proxmox.errors.start_vm_timeout'
    end

    def stop_vm(vm_id)
      vm_info = get_vm_info vm_id
      response = post "/nodes/#{vm_info[:node]}/#{vm_info[:type]}/#{vm_id}/status/stop", nil
      wait_for_completion task_response: response, timeout_message: 'vagrant_proxmox.errors.stop_vm_timeout'
    end

    def shutdown_vm(vm_id)
      vm_info = get_vm_info vm_id
      response = post "/nodes/#{vm_info[:node]}/#{vm_info[:type]}/#{vm_id}/status/shutdown", nil
      wait_for_completion task_response: response, timeout_message: 'vagrant_proxmox.errors.shutdown_vm_timeout'
    end

    def snapshot_qemu_vm(vm_id, node)
      response = post "/nodes/#{node}/qemu/#{vm_id}/snapshot", {snapname: "original", node: node, vmid: vm_id }
      wait_for_completion task_response: response, timeout_message: 'errors.shutdown_vm_timeout'
    end

    def network_qemu_vm(vm_id, node, nic, vlan)
      # random MAC address (starting with "1a" ensures a valid mac that isn't multicast or global - http://sqa.fyicenter.com/1000208_MAC_Address_Validator.html)
      mac = "1a:" + 5.times.map { '%02x' % rand(0..255) }.join(':')
      response = post "/nodes/#{node}/qemu/#{vm_id}/config/", {node: node, vmid: vm_id, net0: "virtio=#{mac},bridge=#{nic},firewall=1,tag=#{vlan.to_s}" }
      wait_for_completion task_response: response, timeout_message: 'vagrant_proxmox.errors.shutdown_vm_timeout'
    end



    private

    # This is called every time for many of the above commands to retrieve the node and vm_type
    # this could be a huge amount of data.
    # @vm_info_cache is used to buffer the info we need
    # We should avoid calling this, and instead pass the node name to the commands
    def get_vm_info(vm_id)
      # only look up each VM once -- and cache the results
      if @vm_info_cache.key? vm_id
        @vm_info_cache[vm_id]
      else
        response = get '/cluster/resources?type=vm'
        @vm_info_cache[vm_id] = response[:data]
          .select { |m| m[:id] =~ /^[a-z]*\/#{vm_id}$/ }
          .map { |m| { id: vm_id, type: /^(.*)\/(.*)$/.match(m[:id])[1], node: m[:node] } }
          .first
      end
    end

    private

    def get_task_exitstatus(task_upid)
      node = /UPID:(.*?):/.match(task_upid)[1]
      response = get "/nodes/#{node}/tasks/#{task_upid}/status"
      response[:data][:exitstatus]
    end

    private

    def get(path)
      response = RestClient::Resource.new("#{api_url}#{path}", :verify_ssl => false).get cookies: { PVEAuthCookie: ticket }
      JSON.parse response.to_s, symbolize_names: true
    rescue RestClient::NotImplemented
      raise ApiError::NotImplemented
    rescue RestClient::InternalServerError => x
      raise ApiError::ServerError, "#{x.message} for GET #{api_url}#{path}"
    rescue RestClient::Unauthorized
      raise ApiError::UnauthorizedError
    rescue => x
      raise ApiError::ConnectionError, x.message
    end

    private

    def delete(path, _params = {})
      response = RestClient.delete "#{api_url}#{path}", headers
      JSON.parse response.to_s, symbolize_names: true
    rescue RestClient::Unauthorized
      raise ApiError::UnauthorizedError
    rescue RestClient::NotImplemented
      raise ApiError::NotImplemented
    rescue RestClient::InternalServerError => x
      raise ApiError::ServerError, "#{x.message} for DELETE #{api_url}#{path}"
    rescue => x
      raise ApiError::ConnectionError, x.message
    end

    private

    def post(path, params = {})
      response = RestClient::Resource.new("#{api_url}#{path}", verify_ssl: false, headers: headers).post params
      JSON.parse response.to_s, symbolize_names: true
    rescue RestClient::Unauthorized
      raise ApiError::UnauthorizedError
    rescue RestClient::NotImplemented
      raise ApiError::NotImplemented
    rescue RestClient::InternalServerError => x
      raise ApiError::ServerError, "#{x.message} for POST #{api_url}#{path}"
    rescue => x
      raise ApiError::ConnectionError, x.message
    end

    private

    def headers
      ticket.nil? ? {} : { CSRFPreventionToken: csrf_token, cookies: { PVEAuthCookie: ticket } }
    end

    private

    def is_file_in_storage?(filename: required('filename'), node: required('node'), storage: required('storage'))
      (list_storage_files node: node, storage: storage).find { |f| f =~ /#{File.basename filename}/ }
    end
  end
end
