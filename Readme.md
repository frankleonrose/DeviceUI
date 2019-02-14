# Device UI
DeviceUI makes it possible to test interactions with your device code running on your local development machine. This avoids the complexity of loading code into a device and enables you to use powerful desktop tools to examine the behavior of your system. Once it's working in DeviceUI, then you can focus on putting it on actual hardware and solving any remaining problems you didn't catch in the Web app.

DeviceUI is a C++ library that you link into your code and build targeting your local machine. The library implements all the Arduino hardware functions so your code can be built for a non-device target. When you run the executable, it starts a Web server. You connect to the Web server with a browser and a Web app appears that shows the value of every pin you

## Development Log

20190131
- Made logging to Javascript console work. It took me an embarrassingly long time to figure out that my `Print` class implementation was using `::printf` to write characters, not `Print::printf` which would delegate output to my `Serial::write()` functions.
- Printing from Logging library comes a single character at a time. Had to create logic that would send serial content to the UI after a number of characters or if we see a newline (\n).
- Broke out MockArduinoUI directory from my LampController project. The git-prune instruction did exactly what I wanted - kept commit history of only the files and that particular subfolder and lifted the contents of the subfolder to the root.

## License

Source code for DeviceUI is released under the MIT License,
which can be found in the [LICENSE](LICENSE) file.

## Copyright

Copyright (c) 2019 Frank Leon Rose
