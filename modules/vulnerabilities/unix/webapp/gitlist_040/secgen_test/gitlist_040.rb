require_relative '../../../../../lib/post_provision_test'

class Gitlist040Test < PostProvisionTest
  REPO = '/home/git/repositories/secret_files'

  def initialize
    self.module_name = 'gitlist_040'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    # The GitList web app is up and serving (tier 2).
    test_service_up
    test_html_returned_content('/', '<title>GitList</title>')

    # Deleted-history leak: opt-in via strings_to_leak_in_history. When a scenario
    # buries a secret in history, prove the mechanism actually worked: the file is
    # gone from HEAD (not browsable in GitList), a later commit deleted it, and its
    # content is still recoverable from history. When the input is absent/blank
    # (the default build and the other gitlist CTFs) these checks SKIP, so the test
    # still passes there.
    #
    # Inputs considered: strings_to_leak_in_history + history_leaked_filenames are
    # checked below. port/strings_to_leak/leaked_filenames/strings_to_pre_leak/
    # pre_leaked_filenames/images_to_leak drive the served web/repo content exercised
    # by test_service_up/test_html above; history_commit_message/
    # history_removal_commit_message only affect commit-message text and are not
    # asserted here.
    secret = self.json_inputs['strings_to_leak_in_history']&.first
    if secret && !secret.strip.empty?
      fname = self.json_inputs['history_leaked_filenames']&.first
      fname = 'secret' if fname.nil? || fname.strip.empty?
      # -C sets the repo dir; safe.directory lets git operate on the www-data repo
      # when the command is run as www-data (avoids "dubious ownership").
      g = "git -C #{REPO} -c safe.directory=#{REPO}"

      tier(2) do
        # Gone from the working tree / HEAD -> not shown by GitList's file browser.
        test_local_command('history secret absent at HEAD',
          "#{g} ls-files -- #{fname} | grep -q . && echo PRESENT || echo ABSENT",
          'ABSENT', user: 'www-data')

        # A commit that *deleted* the file exists -> it was committed then removed.
        test_local_command('history secret removed in a later commit',
          "#{g} log --all --diff-filter=D --oneline -- #{fname} | grep -q . && echo DELETED || echo NODEL",
          'DELETED', user: 'www-data')

        # The content is still recoverable from history (git log -p across the file).
        # Grep for the longest word-ish token in the resolved secret (shell-safe).
        token = secret.scan(/[A-Za-z0-9_]{5,}/).max_by(&:length) || fname
        test_local_command('history secret content recoverable from git history',
          "#{g} log --all -p -- #{fname} | grep -F -- #{token} | head -n1",
          token, user: 'www-data')
      end
    end
  end
end

Gitlist040Test.new.run
