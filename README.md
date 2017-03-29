# Zodiac Application Project

Originally, the main purpose of the project was to create a tool that would help decrypt the infamous 340-character Zodiac Killer cryptogram that remains uncracked to this day (more information at the [Wikipedia page](https://en.wikipedia.org/wiki/Zodiac_Killer#Letters_from_the_Zodiac)). 
However, now the project has a wider application area. 

The project consists of two programs: a desktop program called Zodiac and an Android app called Zodiac Crypt.

#### Desktop program *Zodiac*.

The program is designed for marking up a cipher represented as an image. The markup allows to create a model of the cipher to manipulate symbols in it and to organize decoding process. After the cipher is marked up, the program provides its statistics (symbol frequencies) and tools for decryption. These tools let user replace a symbol in the cipher with a letter by simply clicking on the symbol. There is also a feature to search words in the cipher (i.e. to search such symbol-letter mappings that make the cipher contain the query words if symbols are replaced with corresponding letters).

The source code is located in the */ZodiacDesktopApp* directory. The executable files are places in the root directory (*Zodiac.exe* and *ZodiacApp.jar*). To run the program the latest JRE must be installed on the machine.

*Language: Java. IDE: NetBeans IDE. Created by Dmitry Akishin in 2016.*


#### Android app *Zodiac Crypt*.

Originally, the app was also designed to help decode the Zodiac-340 cipher. Now it is an app where users create their own ciphers and solve cryptograms created by other users.

The source code of one of the versions of the app is located in the */ZodiacAndroidApp* directory.

The app is available on [Google Play](https://play.google.com/store/apps/details?id=com.deakishin.zodiac).

*Language: Java. IDE: Eclipse. Created by Dmitry Akishin in 2016-2017.*
