#!/usr/bin/ruby
require 'base64'
require 'open3'
require 'tmpdir'
require_relative '../../../../../lib/objects/local_string_encoder.rb'
class ImageGenerator < StringEncoder
  attr_accessor :selected_image_path

  def initialize
    super
    self.module_name = 'Random JPG Generator'
    self.selected_image_path = Dir["#{IMAGES_DIR}/misc/*"].sample
  end

  def encode_all
    # Grabs a random PNG from the resources + flattens it onto a white background as a JPG,
    # using the ImageMagick CLI (`magick` in ImageMagick 7, `convert` in ImageMagick 6)
    imagemagick = system('which magick > /dev/null 2>&1') ? 'magick' : 'convert'
    Dir.mktmpdir do |dir|
      tmp_file_path = File.join(dir, 'tmp.jpg')
      out, status = Open3.capture2e(imagemagick, self.selected_image_path, '-background', 'white', '-flatten', tmp_file_path)
      raise "ImageMagick #{imagemagick} failed: #{out}" unless status.success?

      self.outputs << Base64.strict_encode64(File.binread(tmp_file_path))
    end
  end

  def encoding_print_string
    'Random image generator: ' + self.selected_image_path
  end
end

ImageGenerator.new.run
