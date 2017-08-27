Install:
Copy file home-sensors to /etc/init.d and make it executable:
sudo chmod +x home-sensors
Execute:
sudo update-rc.d home-sensors defaults enable

View service status:
sudo systemctl status home-sensors