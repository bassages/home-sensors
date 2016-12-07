Install:
Copy file home-smart-meter to /etc/init.d and make it executable:
sudo chmod +x home-smart-meter
Execute:
sudo update-rc.d home-smart-meter defaults enable

View service status:
sudo systemctl status home-smart-meter