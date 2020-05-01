import copy
import datetime
import json
import logging
import os
import re
import subprocess
import sys
import time
import uuid
import warnings

from elastalert.alerts import Alerter, BasicMatchString, DateTimeEncoder

from elastalert.util import EAException
from elastalert.util import elastalert_logger
from elastalert.util import lookup_es_key
from elastalert.util import pretty_ts
from elastalert.util import resolve_string
from elastalert.util import ts_now
from elastalert.util import ts_to_dt

class ExecAlerter(Alerter):
    required_options = set(['command'])

    def __init__(self, rule):
        super(ExecAlerter, self).__init__(rule)

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
            match_json = match_json.encode()
            input_string = self.rule['name'] + ":||:" + match_json
            stdout, stderr = subp.communicate(input=input_string)
            if self.rule.get("fail_on_non_zero_exit", False) and subp.wait():
               raise EAException("Non-zero exit code while running command %s" % (' '.join(command)))
        except OSError as e:
          raise EAException("Error while running command %s: %s" % (' '.join(command), e))

    def get_info(self):
        return {'type': 'command',
                'command': ' '.join(self.last_command)}