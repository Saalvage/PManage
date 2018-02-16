# PManage

PManage is a simple but powerful CLI-based password manager, it is local, secure and generates passwords that are almost entirely brute force attack proof


## Download

### [Windows](https://drive.google.com/uc?export=download&id=1Hi90noDOXSnW5wpiGBkQPP0LYBwLQxxv)
### [Other](https://drive.google.com/uc?export=download&id=13fYzmtHmRtsslhryCt-QlJOadAvnQNF-)


## Features

* 100% local, no uploading/downloading of your passwords
* Extremely strong AES encryption with a 256 bit key for maximum security
* Password generation that utilizes all 1,114,111 Unicode characters
* Eays to use
* Command-line interface based
* Portable
* Open source


## Basic usage

> new < name > {< length > | < password >}

Creates a password under < name >, this password is either randomly generated using the <length> specified or manually set using the entered < password >

Example: (this creates a password named GitHub with the length 50)
> new GitHub 50

<br>

> get < name >   

Copies password < name > to clipboard