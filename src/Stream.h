#if defined(PLATFORM_NATIVE)

#ifndef STREAM_H
#define STREAM_H

#include "MockArduinoUI.h"
#include "Print.h"
#include <string>
#include <iostream>
#include <sstream>

class Stream : public Print {
  public:
  void begin(int baud) {};
  // void print(char const *s) {
  //   write(s, strlen(s));
  // };
  virtual void flush() {};
  int read() { return -1; };
};

class MockSerial : public Stream {
  std::stringstream _buffer;
  size_t _bufferLength = 0;
  bool _hasLine = false;

  void sendBuffer() {
    std::stringstream s;
    s << "{\"serial\": \"" << _buffer.str() << "\"}";
    send_to_ui(s.str());
    _buffer = std::stringstream();
    _bufferLength = 0;
    _hasLine = false;
  }
  public:

    virtual size_t write(uint8_t c) {
      if (c=='\n') {
        _hasLine = true;
        _buffer << "\\n";
      }
      else if (c=='"') {
        _buffer << "\\\"";
      }
      else {
        _buffer << c;
      }
      ++_bufferLength;
      if (_bufferLength>200 || _hasLine) {
        sendBuffer();
      }
      return 1;
    }
    uint8_t dtr() { return 1; }
    size_t available() { return 0; }
};

typedef MockSerial HardwareSerial;

extern MockSerial Serial;
extern MockSerial Serial1;

#endif
#endif // PLATFORM_NATIVE
