Vagrant.configure("2") do |config|
  config.vm.box = "hashicorp/precise64"
  config.vm.provision :file, source: "src/provision/bin", destination: "bin"
  config.vm.provision :file, source: "src/provision/sql", destination: "sql"
  config.vm.provision :shell, inline: "./bin/bootstrap.sh"
  config.vm.network "private_network", ip: "192.168.7.27"
end
