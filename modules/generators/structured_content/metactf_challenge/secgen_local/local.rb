#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'

require 'json'
require 'securerandom'

class MetaCTFChallengeGenerator < StringGenerator
  attr_accessor :challenge_path
  attr_accessor :difficulty
  attr_accessor :flag
  attr_accessor :group
  attr_accessor :existing_challenges

  def initialize
    super
    self.module_name = 'MetaCTF Challenge Generator'
    self.challenge_path = ''
    self.difficulty = ''
    self.flag = ''
    self.group = ''
    self.existing_challenges = []
  end


  def get_options_array
    super + [['--challenge_path', GetoptLong::OPTIONAL_ARGUMENT],
             ['--difficulty', GetoptLong::REQUIRED_ARGUMENT],
             ['--flag', GetoptLong::REQUIRED_ARGUMENT],
             ['--group', GetoptLong::OPTIONAL_ARGUMENT],
             ['--existing_challenges', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--challenge_path'
      self.challenge_path << arg;
    when '--difficulty'
      self.difficulty << arg;
    when '--flag'
      self.flag << arg;
    when '--group'
      self.group << arg;
    when '--existing_challenges'
      self.existing_challenges << arg;
    end
  end

  def generate

    # TODO : run through the challenges and adjust difficulty to something more appropriate
    src_angr = [
        {:difficulty => 'medium', :path => 'src_angr/00_angr_find'},
        {:difficulty => 'medium', :path => 'src_angr/01_angr_avoid'},
        {:difficulty => 'medium', :path => 'src_angr/02_angr_find_condition'},
        {:difficulty => 'medium', :path => 'src_angr/03_angr_symbolic_registers'},
        {:difficulty => 'medium', :path => 'src_angr/04_angr_symbolic_stack'},
        {:difficulty => 'medium', :path => 'src_angr/05_angr_symbolic_memory'},
        {:difficulty => 'medium', :path => 'src_angr/06_angr_symbolic_dynamic_memory'},
        {:difficulty => 'medium', :path => 'src_angr/07_angr_symbolic_file'},
        {:difficulty => 'medium', :path => 'src_angr/08_angr_constraints'},
        {:difficulty => 'hard', :path => 'src_angr/09_angr_hooks'},
        {:difficulty => 'hard', :path => 'src_angr/10_angr_simprocedures'},
        {:difficulty => 'hard', :path => 'src_angr/11_angr_sim_scanf'},
        {:difficulty => 'hard', :path => 'src_angr/12_angr_veritesting'},
        {:difficulty => 'hard', :path => 'src_angr/13_angr_static_binary'},
        {:difficulty => 'hard', :path => 'src_angr/14_angr_shared_library'},
        # TODO: Replacing 'Good Job with a printflag(); call requires more thought for task 15 - omit for now
        # '{:difficulty => 'easy', :path => 'src_angr/15_angr_arbitrary_read'},
        {:difficulty => 'hard', :path => 'src_angr/16_angr_arbitrary_write'},
        {:difficulty => 'hard', :path => 'src_angr/17_angr_arbitrary_jump'},
    ]

    src_csp = [
        {:difficulty => 'easy', :path => 'src_csp/Ch1-2/Ch1_Ltrace'},
        {:difficulty => 'easy', :path => 'src_csp/Ch1-2/Ch1_Readelf'},
        {:difficulty => 'easy', :path => 'src_csp/Ch1-2/Ch2_01_Endian'},
        {:difficulty => 'easy', :path => 'src_csp/Ch1-2/Ch2_01_Showkey'},
        {:difficulty => 'easy', :path => 'src_csp/Ch1-2/Ch2_03_IntOverflow'},
        {:difficulty => 'easy', :path => 'src_csp/Ch1-2/Ch2_03_TwosComplement'},
        {:difficulty => 'medium', :path => 'src_csp/Ch1-2/Ch2_03_XorInt'},
        {:difficulty => 'medium', :path => 'src_csp/Ch1-2/Ch2_05_FloatConvert'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3-ExtraFormatStr/Ch3_Format0_Leak'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3-ExtraFormatStr/Ch3_Format1_LeakDollar'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3-ExtraFormatStr/Ch3_Format2_nCorruptKey'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3-ExtraFormatStr/Ch3_Format3_nWriteKey'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3-ExtraFormatStr/Ch3_Format4_nWriteFnP'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3-ExtraFormatStr/Ch3_Format5_nTargetWrite'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3-ExtraFormatStr/Ch3_Format6_PLTHijack'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3.1-3.5/Ch3_00_GdbIntro'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3.1-3.5/Ch3_00_GdbRegs'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3.1-3.5/Ch3_00_GdbSetmem'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3.1-3.5/Ch3_01_GdbPractice'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3.1-3.5/Ch3_02_AsciiInstr'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3.1-3.5/Ch3_04_FnPointer'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3.1-3.5/Ch3_04_LinkedList'},
        {:difficulty => 'medium', :path => 'src_csp/Ch3.1-3.5/Ch3_05_XorLong'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.1-3.5/Ch3_05_XorStr'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.6-3.7/Ch3_06_Conditionals'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.6-3.7/Ch3_06_LoopMulti'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.6-3.7/Ch3_06_SwitchTable'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.6-3.7/Ch3_07_FloatReturn'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.6-3.7/Ch3_07_ParamsRegs'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.6-3.7/Ch3_07_ParamsStack'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.6-3.7/Ch3_07_SegvBacktrace'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.6-3.7/Ch3_07_StaticInt'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.6-3.7/Ch3_07_StaticStrcmp'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.7-3.9/Ch3_07_CanaryBypass'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.7-3.9/Ch3_07_HijackPLT'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.7-3.9/Ch3_07_ScanfOverflow'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.7-3.9/Ch3_07_StackSmash'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.7-3.9/Ch3_08_2DArrays'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.7-3.9/Ch3_08_Matrix'},
        {:difficulty => 'hard', :path => 'src_csp/Ch3.7-3.9/Ch3_09_Structs'},
        {:difficulty => 'hard', :path => 'src_csp/Ch5-8/Ch5_08_LoopUnroll'},
        {:difficulty => 'hard', :path => 'src_csp/Ch5-8/Ch7_13_LdPreloadGetUID'},
        {:difficulty => 'hard', :path => 'src_csp/Ch5-8/Ch8_05_PsSignals'},
        {:difficulty => 'hard', :path => 'src_csp/Ch5-8/Ch8_05_Signals'},
    ]

    src_malware = [
        {:difficulty => 'easy', :path => 'src_malware/Ch01-08/Ch01StatA_Readelf'},
        {:difficulty => 'easy', :path => 'src_malware/Ch01-08/Ch03DynA_Ltrace'},
        {:difficulty => 'easy', :path => 'src_malware/Ch01-08/Ch04x86_AsciiInstr'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch04x86_AsciiStrcmp'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch06CAsm_Conditionals'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch06CAsm_LinkedList'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch06CAsm_LoopMulti'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch06CAsm_SwitchTable'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch08Dbg_GdbIntro'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch08Dbg_GdbParams'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch08Dbg_GdbPractice'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch08Dbg_GdbRegs'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch08Dbg_GdbSetmem'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch08Dbg_InputFormat'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch08Dbg_Radare2Intro1'},
        {:difficulty => 'medium', :path => 'src_malware/Ch01-08/Ch08Dbg_Radare2Intro2'},
        {:difficulty => 'hard', :path => 'src_malware/Ch01-08/Ch08Dbg_StaticInt'},
        {:difficulty => 'hard', :path => 'src_malware/Ch01-08/Ch08Dbg_StaticRE'},
        {:difficulty => 'hard', :path => 'src_malware/Ch01-08/Ch08Dbg_StaticStrcmp'},
        {:difficulty => 'hard', :path => 'src_malware/Ch11-13/Ch11MalBeh_HijackPLT'},
        {:difficulty => 'hard', :path => 'src_malware/Ch11-13/Ch11MalBeh_LdPreloadGetUID'},
        {:difficulty => 'hard', :path => 'src_malware/Ch11-13/Ch11MalBeh_LdPreloadRand'},
        {:difficulty => 'hard', :path => 'src_malware/Ch11-13/Ch11MalBeh_NetcatShovel'},
        {:difficulty => 'hard', :path => 'src_malware/Ch11-13/Ch12Covert_ForkFollow'},
        {:difficulty => 'hard', :path => 'src_malware/Ch11-13/Ch12Covert_ForkPipe'},
        {:difficulty => 'hard', :path => 'src_malware/Ch11-13/Ch13DataEnc_BaseEnc'},
        {:difficulty => 'hard', :path => 'src_malware/Ch11-13/Ch13DataEnc_XorEnc'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch15AntiDis_FakeCallInt'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch15AntiDis_FakeCond'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch15AntiDis_FakeMetaConds'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch15AntiDis_InJmp'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch15AntiDis_PushRet'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch16AntiDbg_BypassPtrace'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch16AntiDbg_GdbCheckTrace'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch16AntiDbg_Int3Scan'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch16AntiDbg_SigtrapCheck'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch16AntiDbg_SigtrapEntangle'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch16AntiDbg_SigtrapHijack'},
        {:difficulty => 'hard', :path => 'src_malware/Ch15-16/Ch16AntiDbg_TimeCheck'},
        {:difficulty => 'hard', :path => 'src_malware/Ch18-21/Ch18PackUnp_UnpackEasy'},
        {:difficulty => 'hard', :path => 'src_malware/Ch18-21/Ch18PackUnp_UnpackGdb'},
        {:difficulty => 'hard', :path => 'src_malware/Ch18-21/Ch21x64_ParamsRegs'},
        {:difficulty => 'hard', :path => 'src_malware/Ch18-21/Ch21x64_ParamsStack'},
    ]

    challenges = src_angr + src_csp + src_malware

    if self.existing_challenges != []
      Print.local("Removing #{self.existing_challenges.size.to_s} existing challenge(s)")
      self.existing_challenges.each do |json_challenge|
        challenge = JSON.parse(json_challenge)
        challenges.delete_if do |cha|
          cha[:path] == challenge['challenge_path']
        end
      end
    end

    if self.challenge_path != ''
      challenges.delete_if do |challenge|
        challenge[:path] != self.challenge_path
      end
      challenge = challenges.first
    elsif self.challenge_path == '' and self.difficulty != ''
      challenges.delete_if do |challenge|
        challenge[:difficulty] != self.difficulty
      end

      challenge = challenges.sample
    else
      # throw an errorr
      Print.err('Error: MetaCTF Challenge Generator. ')
      Print.err('Either provide a challenge path or a difficulty.') # TODO: more meaningful error messages here
      exit(1)
    end

    if self.group == ''
      self.group = SecureRandom.hex.slice(1..8)
    end


    outputs << {'challenge_name' => challenge[:path].split('/')[-1], 'challenge_path' => challenge[:path], 'flag' => self.flag, 'group' => self.group }.to_json

  end

end

MetaCTFChallengeGenerator.new.run