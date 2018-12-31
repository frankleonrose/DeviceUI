#if defined(PLATFORM_NATIVE)

#include "Arduino.h"
#include "MockArduinoUI.h"
#include <chrono>
#include <cstdlib>
// #include <functional>
#include <iostream>
#include <vector>

MockSerial Serial;
MockSerial Serial1;

void delay(uint16_t msec) {
  std::cerr << "We're calling delay(): " << msec << std::endl;
};

void delayMicroseconds(uint32_t usec) {
  std::cerr << "We're calling delayMicroseconds(): " << usec << std::endl;
};

auto start = std::chrono::steady_clock::now();
unsigned long millis() {
  auto end = std::chrono::steady_clock::now();
  auto int_ms = std::chrono::duration_cast<std::chrono::milliseconds>(end-start);
  return static_cast<unsigned long>(0xFFFFFFFF & int_ms.count()); // Just lowest 32 bits
};
uint32_t micros() {
  auto end = std::chrono::steady_clock::now();
  auto int_us = std::chrono::duration_cast<std::chrono::microseconds>(end-start);
  return static_cast<unsigned long>(0xFFFFFFFF & int_us.count()); // Just lowest 32 bits
};
void interrupts() {};
void noInterrupts() {};

const int nPins = 120;
std::vector<int> pinValues(nPins, 0);
std::vector<uint8_t> pinModes(nPins, INPUT);

void pinMode(uint8_t pin, uint8_t mode) {
  std::string mode_string;
  switch (mode) {
    case INPUT: mode_string = "input"; break;
    case OUTPUT: mode_string = "output"; break;
    default: mode_string = "unknown_mode"; break;
  }
  std::string json = "{\"op\":\"pinMode\",\"pin\":" + std::to_string(pin) + ",\"mode\":\"" + mode_string + "\"}";
  send_to_ui(json);
};
void digitalWrite(uint8_t pin, uint8_t val) {
  std::string json = "{\"op\":\"digitalWrite\",\"pin\":" + std::to_string(pin) + ",\"value\":\"" + std::to_string(val) + "\"}";
  send_to_ui(json);
  if (pinModes[pin]==INPUT) {
    std::cerr << "Writing to INPUT pin " << pin << std::endl;
  }
};
void set_pin_state(int pin, int value) {
  pinValues[pin] = value;
}
int digitalRead(uint8_t pin) {
  return pinValues[pin]==0 ? 0 : 1;
};
int analogRead(uint8_t pin) {
  return pinValues[pin];
}
void analogReadResolution(int) {}

long nativeRandom(long max) {
  return max / 2;
}

size_t Print::print(const char value[]) {
  return printf("%s", value);
}
size_t Print::print(char value) {
  return printf("%c", value);
}
size_t Print::print(unsigned char value, int base) {
  return printf(base==HEX ? "%x" : "%u", (unsigned)value);;
}
size_t Print::print(int value, int base) {
  return printf(base==HEX ? "%x" : "%d", (int)value);;
}
size_t Print::print(unsigned int value, int base) {
  return printf(base==HEX ? "%x" : "%u", (unsigned)value);;
}
size_t Print::print(long value, int base) {
  return printf(base==HEX ? "%x" : "%d", (int)value);;
}
size_t Print::print(unsigned long value, int base) {
  return printf(base==HEX ? "%lx" : "%lu", value);;
}
size_t Print::print(double value, int base) {
  return printf("%lf", value);
}

#endif // PLATFORM_NATIVE
