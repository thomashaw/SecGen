from elastalert.alerts import Alerter, BasicMatchString

class ExecAlerter(Alerter):
    required_options = set(['command'])

    def __init__(self, *args):
        super(ExecAlerter, self).__init__(*args)

        self.last_command = []

        self.shell = False
        if isinstance(self.rule['command'], str):
            self.shell = True
            if '%' in self.rule['command']:
                logging.warning('Warning! You could be vulnerable to shell injection!')
            self.rule['command'] = [self.rule['command']]

        self.new_style_string_format = False
        if 'new_style_string_format' in self.rule and self.rule['new_style_string_format']:
            self.new_style_string_format = True

    def alert(self, matches):
        # Format the command and arguments
        try:
            command = [resolve_string(command_arg, matches[0]) for command_arg in self.rule['command']]
            self.last_command = command
        except KeyError as e:
            raise EAException("Error formatting command: %s" % (e))

        # Run command and pipe data
        try:
            subp = subprocess.Popen(command, stdin=subprocess.PIPE, shell=self.shell)
                  match_json = json.dumps(matches, cls=DateTimeEncoder) + '\n'
                  json = match_json.encode()
                  input_string = self.rule['name'] + ":" + json
                  stdout, stderr = subp.communicate(input=input_string)
              if self.rule.get("fail_on_non_zero_exit", False) and subp.wait():
                 raise EAException("Non-zero exit code while running command %s" % (' '.join(command)))
        except OSError as e:
          raise EAException("Error while running command %s: %s" % (' '.join(command), e))

    def get_info(self):
        return {'type': 'command',
                'command': ' '.join(self.last_command)}