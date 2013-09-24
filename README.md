android-manet-manager
=====================

SPAN - Android Manet Manager

Google Play:
https://play.google.com/store/apps/details?id=org.span

Obtaining Custom Kernels
========================
For many devices, the kernel must be modified to allow ad-hoc mode before android-manet-manager will work. There are 
kernels available for several devices at this repository:

https://github.com/monk-dot/SPAN/tree/master/kernels

How to create a JAR to include MANET Manager in your project
============================================================
- Import android-manet-manager into Eclipse
- Right click on 'AndroidManetManager' in the Package Explorer and click 'Export'
- Select 'JAR file' under 'Java' and click 'Next'
- Uncheck 'libmanet.jar' and 'AndroidManifest.xml' in the right pane, and uncheck the 'bin/', 'libs/', and 'obj/'folders in the left pane
- Set the exported file destination to be the libs/ folder of your project
- Click 'Finish'
