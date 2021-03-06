#ifndef MOCK_ARDUINO_UI_H
#define MOCK_ARDUINO_UI_H

#pragma once

void send_to_ui(std::string message);

void set_pin_state(int pin, int value);

void setup(void);
void loop(void);

#endif
