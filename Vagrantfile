# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "precise64"
  config.vm.box_url = "http://files.vagrantup.com/precise64.box"
  config.vm.hostname = "vagrant.robot.to"

  # config.vm.network :forwarded_port, guest: 80, host: 8080
  config.vm.network :forwarded_port, guest: 27017, host: 27017  # MongoDB

  # config.vm.network :public_network
  config.vm.provider "virtualbox" do |v|
    v.customize ["modifyvm", :id, "--memory", 1024]
  end

  ###################
  # Provisioning
  ###################

  config.vm.provision :chef_solo do |chef|
     chef.cookbooks_path = "provision/vagrant/cookbooks"
     chef.roles_path     = "provision/vagrant/roles"
     chef.data_bags_path = "provision/vagrant/data_bags"
     chef.add_role "web"
  end

end
