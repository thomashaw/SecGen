require 'nokogiri'
require 'digest'

class XMLReader

  # uses nokogiri to extract all system information from scenario.xml
  # This includes module filters, which are module objects that contain filters for selecting
  # from the actual modules that are available
  # @return [Array] Array containing Systems objects

  def self.parse_doc(file_path, schema, type)
    doc = nil
    begin
      doc = Nokogiri::XML(File.read(file_path))
    rescue
      Print.err "Failed to read #{type} configuration file (#{file_path})"
      exit
    end
    validate_xml(doc, file_path, schema, type)
    # remove xml namespaces for ease of processing
    doc.remove_namespaces!
  end

  def self.validate_xml(doc, file_path, schema, type)
    # validate XML against schema
    begin
      xsd = Nokogiri::XML::Schema(File.open(schema))
      xsd.validate(doc).each do |error|
        Print.err "Error in scenario configuration file (#{scenario_file}):"
        Print.err "    #{error.line}: #{error.message}"
        exit
      end
    rescue Exception => e
      Print.err "Failed to validate #{type} xml file (#{file_path}): against schema (#{schema})"
      Print.err e.message
      exit
    end

  end

  def self.read_attributes(node)
    attributes = {}
    node.xpath('@*').each do |attr|
      attributes["#{attr.name}"] = [attr.text] unless attr.text.nil? || attr.text == ''
    end
    attributes
  end

end