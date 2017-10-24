# DeeplinkModule
Add request handlers to your app which will trigger microflows.

## Typical Usage Scenario

With this module you can configure links to trigger microflows such as: 

* http://myapp.com/link/user/Michel
* http://myapp.com/link/product/22
* https://myapp.com/link/resetpassword/DF6345SDF
* https://myapp.com/link/allusers

The module is design- and runtime configurable and respects security and support links for both logged in- and anonymous users.

* Create persistent links to view only forms, which you can use in emails, on your website etcetera.
* Provide a colleague a link to a certain object, instead of describing the necessary navigation steps.
* Generate confirmation links which can be emailed to the user.

## Contributing
For more information on contributing to this repository visit [Contributing to a GitHub repository](https://docs.mendix.com/howto/collaboration-project-management/contribute-to-a-github-repository)!

## Configuration
After importing the module into your application you need to configure it. 

### Initialize DeepLink Module on App Startup
The ```/link/``` path needs to be added as a request handler in your app. This will be covered when you add the ```DeepLink.Startdeeplink``` microflow to the [startup microflow]('https://docs.mendix.com/refguide/project-settings#after-startup') of your application.

To automatically start the deeplink module, the DeepLink.Startdeeplink microflow needs to be set as startup microflow (Desktop Modeler > Project > Settings > Server > After startup). If you already have a startup microflow configured in your application you need to extend it with a submicroflow activity which calls the ```DeepLink.Startdeeplink``` microflow.

### Security 
All roles that need to be able to change the configuration of the deeplink module (at runtime) require the *DeepLink.Admin* userrole (Desktop Modeler > Project > Security > User roles).

All other roles, including your guest roles, should have the *DeepLink.User* userrole. Otherwise they will not be able to use any link.

### Navigation
All roles that need to be able to change the configuration of the deeplink module (at runtime) should be able to reach a page which includes the *DeepLink.DeepLinkConfigurationOverview* snippet.


### Adding Deep Link entries
The available deeplinks can be configured both design time and/or runtime. 
It is in general, and especially when you're starting with the Deeplink module, easier to manage deeplinks at runtime. In run time you are provided with suggestions for the parameters that need to be configured for a deep link entry. 

#### Design time
You need a custom microflow with 'Call microflow' activities which call the *DeepLink.CreateDeeplinkConfig* microflow. You need a microflow call for every deep link entry you want to configure. 

#### Run time
Start the application and log in with a user which has the Deeplink.Admin modulerole assocoated to one of his userroles.

Open the page which includes the *DeepLink.DeepLinkConfigurationOverview* snippet(See Navigation).

On this page you can manage all deeplink configuration entries. 

The *Deeplink.CreateDeeplinkConfig* microflow requires five parameters to be set carefully:

#### Deeplink parameters

* _**Name**_ - The name of the link, as seen by the user of your app. If name is set to 'product' for example, the generated deeplink results in: http://yourhost/link/product/17

* _**Microflow**_ - The fully qualified name of the microflow that will be invoked by this deeplink. For example: *MyFirstModule.ShowProduct*

* _**Use string argument**_ - 
If the selected microflow has a single string argument, the remainder of the invoking URL will be passed as argument to the microflow. This way it is possible to invoke microflows without having a corresponding object in the database. This property cannot be used in combination with the object type and object attribute properties.

* _**Include GET parameters**_ - If using a String argument, the GET parameters following the URL will also be included in the String passed to the microflow. Example: with a URL like "http://appname/link/mfname/stringtext?param=value", previously only the String "stringtext" would be passed to the microflow. When this option is enabled, the GET parameters will be added. The String passed to the microflow will now be "stringtext?param=value". Of course, multiple GET parameters (using "&") also work. This property cannot be used in combination with the object type and object attribute properties.

* _**Separate GET parameters**_ - If GET parameters are included, they will be separated into multiple String parameters for the microflow that will be called. Example: with a URL like "http://appname/link/mfname/stringtext?param=value&other=test", The microflow that is called can receive two String parameters named "param" and "other", that will be filled with the values "value" and "test" respectively.  When creating a deeplink in the GUI, an example URL will be shown for the selected microflow. This property cannot be used in combination with the object type and object attribute properties.

* _**Object Type**_ - The fully qualified type of the object which need to be passed to the microflow. If empty, no arguments will be passed to the microflow. For example: MyFirstModule.Product.

* _**Object Attribute**_ - The attribute that will be used by the deeplink to uniquely identify the object that needs to be passed. In case of this value being configured empty, GUIDs will be used. when the object type is set to the entity 'User' and object attribute to 'Name',for example, you can use links such as http://yourhost/link/showuser/[randomUserName]
where randomUsername is the value of the attribute Name of entity User.

* _**Allow guests**_ - Allow anonymous users to use this deeplink.

* _**Use as Home**_ - When requesting a certain deep link the deeplink will be reused when the user reloads the application. This way an alternative dashboard can be presented when the user enters the application by requesting a deeplink. Defaults to 'false'.

* _**Alternative index page**_ - This causes a deeplink to not use the default index page. Using this property alternative themes can be applied when requesting certain deeplinks. For example 'index-dark.html'.


### Handling DeepLink Requests
After handling a request the DeepLink module will redirect to the homepage of your application. The homepage is configured in the [navigation](https://docs.mendix.com/refguide/navigation).

 Instead of opening the default homepage the DeepLink module needs to figure out what microflow is associated with the requested deep link. For this you need to change the default homepage in your navigation to a custom microflow. 

 If default homepage is already a microflow, you should modify it.

 The first activity in this custom microflow has to be a *Call microflow* activity which calls *Deeplink.DeeplinkHome*.
 This microflow returns a boolean value which indicates if the deeplink module will start triggering a microflow.
 Add an exclusive split which handles the result of *Deeplink.DeeplinkHome*. 
 
 * When the result of Deeplink.DeeplinkHome is ```true``` the custom microflow should end. The DeepLink module will take of calling the correct microflow.

 * When the result is ```false``` the microflow should continue with an ```Open Page``` activity which opens the page or microflow which is your default home page.(The original intended behavior).

 ### Constants (Optional)
 
 * _**IndexPage**_ - In special cases, for example when you want to load a specific theme or bypass a certain single sign on page, you can modify this constant to redirect to another index page like 'index3.html' or 'index-mytheme.html'.

* _**LoginLocation**_ - If a user session is required this constant defines the loginpage where the user is supposed to enter the login credentials. This property is useful in single-sign-on environments. If empty, the default Mendix built-in login page is used. If not empty, it is assumed that after login, the user will be redirected to the deeplink again. For this reason the provided url is appended with the original deeplink. For example:
'https://mxid.mendix.com/login?a=MyApp&f=true&cont=' 
or
'../sso/login?f=true&cont=' 




