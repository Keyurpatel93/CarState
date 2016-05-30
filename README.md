#CarState

Please note that this application is a complementary application for the [MyCar](https://github.com/Keyurpatel93/MyCar) application to show current car settings, and settings after a user has been authenticated using the MyCar application. 

The CarState applications were developed to be an extension of the [OpenXC](http://openxcplatform.com) car emulator web application. The OpenXC car emulator application does not allow data to be written to it, therefore in order to demo how car personalization would work, a new application needed to be created. This application wraps additionally functional to enable car personalization as it can store values for a driver’s seat position and radio station presets.  To change the seating position of the car, a user simply clicks towards the front of the seat to move the seat forward or towards the back of the seat image to move the seat back on the application. All other settings can be changed using the OpenXC web application such as the car state (park, reverse, drive), the gas peddle and break peddle position etc. 



## Functionality:
*	Allows a driver/user to change seat position.
*	Displays the max speed limit set for a user and if the speed limit is exceeded displays an alert. 
*	Display if the seatbelt enforcement setting is activated for a user. 


## Dependencies:
- **Hardware**:
  - Android Device with a fingerprint sensor running Android 6.0 or later.
- **Software**: 
  - [OpenXC](http://openxcplatform.com/android/getting-started.html)



##Permissions Needed:
- android.permission.WRITE_EXTERNAL_STORAGE
- android.permission.READ_EXTERNAL_STORAGE


Need read/write permission to read and write to a xml file that stores data for the application. The xml file called “cardata.xml” which is located at the root of the internal storage, stores settings data (seat position, constraints) for enrolled users.

## License
Licensed under the BSD license.

##Screenshot
![Alt text](https://github.com/Keyurpatel93/CarState-/blob/master/CarState.png?raw=true "Screenshot")
