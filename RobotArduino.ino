//#include <Servo.h>

int outputPins[] = {11, 10, 9, 6, 5};
//Servo base, shoulder, elbow, wrist, grip;

void setup() {
  // initialize Serial w/ baud rate 9600
  Serial.begin(9600);
  
  // attach servos to pins
  /*base.attach(outputPins[0]);
  shoulder.attach(outputPins[1]);
  elbow.attach(outputPins[2]);
  wrist.attach(outputPins[3]);
  grip.attach(outputPins[4]);*/
}

void loop() {
  // look for serial data
  if(Serial.available()) {
    // load it into a buffer - chars are equivalent to java bytes
    char received[5];
    Serial.readBytes(received, 5);
    
    // convert the chars into bytes representing angles
    byte converted[5];
    for(int i=0; i<5; i++) {
      converted[i] = map(received[i], -128, 127, 0, 180);
    }
    
    // test to see if the sent data is what is expected
    if(converted[0] == 180 && converted[1] == 0 && converted[2] == 90 && converted[3] == 0 && converted[4] == 180) {
      Serial.println("Message Received Correctly");
    }
    
    // write the angles to the servos
    /*base.write(converted[0]);
    shoulder.write(converted[1]);
    elbow.write(converted[2]);
    wrist.write(converted[3]);
    grip.write(converted[4]);*/
  }
}
