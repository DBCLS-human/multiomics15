lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)

require 'json'
metadata = open('./metadata.json') do |io|
  JSON.load(io)
end

Gem::Specification.new do |spec|
  spec.name          = 'beacon_stanza'
  spec.version       = '0.0.1'
  spec.authors       = ''
  spec.email         = Array(metadata["stanza:address"])
  spec.summary       = metadata["stanza:label"]
  spec.description   = metadata["stanza:definition"]
  spec.homepage      = ''
  spec.license       = 'MIT License'

  spec.files         = Dir.glob('**/*')
  spec.executables   = spec.files.grep(%r{^bin/}) { |f| File.basename(f) }
  spec.test_files    = spec.files.grep(%r{^(test|spec|features)/})
  spec.require_paths = ['lib']

  spec.add_runtime_dependency 'togostanza'
end
