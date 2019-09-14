# Common
common operation for any project
add library->

allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  dependencies {
	        implementation 'com.github.vikrant1993:Common:1.0.0'
	}
