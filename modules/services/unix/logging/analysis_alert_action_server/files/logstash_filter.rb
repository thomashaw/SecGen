# StateSentry filter-based detection
# Drop this at /etc/logstash/filter_detection.rb

def register(params)
  @hostname = 'tom-noekea-2-experiment-aaa-hackme-desktop'
  @router   = '/usr/bin/ruby /opt/alert_actioner/alert_router.rb raise --alert-name'
  @read_executables = [
    '/bin/cat', '/usr/bin/vim.basic', '/usr/bin/less',
    '/bin/more', '/bin/nano', '/usr/bin/kate'
  ]
  @acc_actions = ['user_login', 'started-session', 'acquired-credentials', 'opened-file']
end

def fire(event, alert_name)
  event.set('filter_alert_name', alert_name)
  event.set('filter_detection_time', Time.now.utc.strftime('%Y-%m-%dT%H:%M:%S.%6NZ'))
  system("sudo -u aaa_admin /usr/bin/ruby /opt/alert_actioner/alert_router.rb raise --alert-name #{alert_name}")
end

def filter(event)
  begin
    action        = event.get('[event][action]')
    result        = event.get('[auditd][result]')
    combined_path = event.get('combined_path')
    executable    = event.get('[process][executable]')
    user_name     = event.get('[user][name]')
    related_users = event.get('[related][user]')

    # ── readable_shadow-rf-0 ──────────────────────────────────────────────
    if action == 'opened-file' &&
       result == 'success' &&
       combined_path == '/etc/shadow' &&
       @read_executables.include?(executable)
      fire(event, "#{@hostname}-readable_shadow-rf-0")
    end

    # ── hidden_file-rf-0 ──────────────────────────────────────────────────
    if action == 'opened-file' &&
       result == 'success' &&
       combined_path == '/home/challenger/.top_secret_file' &&
       @read_executables.include?(executable)
      fire(event, "#{@hostname}-hidden_file-rf-0")
    end

    # ── hackme_desktop-acc-0 ──────────────────────────────────────────────
    crackme_match = (user_name == 'crackme') ||
                    (related_users.is_a?(Array) && related_users.include?('crackme')) ||
                    (related_users.is_a?(String) && related_users == 'crackme')

    if @acc_actions.include?(action) &&
       result == 'success' &&
       crackme_match
      fire(event, "#{@hostname}-hackme_desktop-acc-0")
    end

    # ── distcc_exec-acc-0 ─────────────────────────────────────────────────
    distccd_match = (user_name == 'distccd') ||
                    (related_users.is_a?(Array) && related_users.include?('distccd')) ||
                    (related_users.is_a?(String) && related_users == 'distccd')

    if @acc_actions.include?(action) &&
       result == 'success' &&
       distccd_match
      fire(event, "#{@hostname}-distcc_exec-acc-0")
    end

  rescue => e
    event.set('filter_detection_error', e.message)
  end

  [event]
end
