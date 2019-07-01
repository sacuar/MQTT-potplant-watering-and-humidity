//@copyright_ Muhammed Sacuar Hussain(University of duisburg essen, Germany)
//MQTT humidity control
//
import mqtt.*;

  private PFont fontA;
  MQTTClient client;
  private String MQTT_BROKER ="tcp://192.168.0.150:1883";
  private String CLIENT_ID = "TestProcessing";
  private int QOS = 1;
  private String TOPICS = "humidity";
  private String PUMPON = "1";
  private String PUMPOFF = "0";
  private boolean CLEAN_START = true;
  private boolean RETAINED = true;
  private short KEEP_ALIVE = 30;
  private String publish_topic="waterpump";
  private int t=0;
  private int height_diagramm=200;
  private int width_diagramm=300;
  private int diagramm_x_start=150;
  private int diagramm_y_start=150;
  private float framerate=2;
  private float delta_t=0;
  private float duration_diagramm=width_diagramm*delta_t;
  
  private int soil_dry=300;
  private int hysteresis=200;
  
  private int circleXon=width/4;
  private int circleYon=diagramm_y_start+height_diagramm+100;
  private int circleXoff=3*(width/4);
  private int circleYoff=diagramm_y_start+height_diagramm+100;
  private int circleSize=50;
  private boolean circleonOver=false;
  private boolean circleoffOver=false;
  private boolean pumpon_bool=false;
  private boolean automatic_on=false;
  ArrayList<Points> poop = new ArrayList();


  public String current;

  public void setup() {
    size(500, 500);
    circleXon=width/4;
    circleXoff=3*(width/4);
    delta_t = 1/framerate;
    duration_diagramm= width_diagramm * delta_t;
    Points P1 = new Points(diagramm_x_start,height_diagramm+diagramm_y_start);
    poop.add(P1);
    Points P2 = new Points(diagramm_x_start+width_diagramm,height_diagramm+diagramm_y_start);
    poop.add(P2);
    frameRate(framerate);
    ellipseMode(CENTER);
    drawStuff();
    
     //set up broker connection
    client = new MQTTClient(this);
    client.connect(MQTT_BROKER, CLIENT_ID, CLEAN_START);
    client.subscribe(TOPICS, QOS);
    current = "0";
  }

  public void draw() {
    drawStuff();
    noFill();
    stroke(0);   
    beginShape();
    for (int i=0;i<poop.size();i++) {
      Points P = (Points)poop.get(i);
      vertex(P.x, P.y);
      if (P.x<diagramm_x_start)poop.remove(i);
      P.x--;
    }
    endShape();
    
  }

  void drawStuff() {
    background(-1);
    fill(0);
    textAlign(CENTER);
    
    text("Smart Plantpot Current Humidity",312,20);      
    fill(0, 102, 153, 204);
    text("MQQT Broker:piiot",315,35);
  int s = second(); 
  int m = minute(); 
  int h = hour();
 text(h+":"+m+":"+s,328,53);
  fill(0, 102, 153, 204);
    //fill(0,255,0);
    text("Time/s",diagramm_x_start+(width_diagramm/2),height_diagramm+diagramm_y_start+40);
    stroke(0);
    line(diagramm_x_start,diagramm_y_start+height_diagramm,diagramm_x_start+width_diagramm,diagramm_y_start+height_diagramm);
    line(diagramm_x_start, diagramm_y_start, diagramm_x_start, diagramm_y_start+height_diagramm);
    for (int i = 0+diagramm_x_start; i <=diagramm_x_start+width_diagramm ; i += 50) {
      fill(0, 255, 0);
      int seconds=int(map(i,diagramm_x_start,diagramm_x_start+width_diagramm,duration_diagramm,0));
      text(str(-seconds), i, diagramm_x_start+height_diagramm+15);
    }
    for (int j = 0+diagramm_y_start; j <= diagramm_y_start+height_diagramm; j += 50) {
      fill(0, 255, 0);
      int value=int(map(j,diagramm_y_start,diagramm_y_start+height_diagramm,1023,0));
      text(value, diagramm_x_start-15, j);
    }
    
    fill(0, 102, 153, 204);
    text("Humidity",diagramm_x_start-70,diagramm_y_start+(height_diagramm/2));
    //rotate(-PI/2);
    stroke(0);
    update();
    if (circleonOver) {
      fill(0,255,0);
    } 
    else {
      fill(255,255,255);
    }
    ellipse(circleXon,circleYon,circleSize,circleSize);
    if (pumpon_bool){
      fill(0,255,0);
    }
    else{
      fill(0);
    }
    text("ON", circleXon,circleYon+5);
    if (circleoffOver) {
      fill(255,0,0);
    } 
    else {
      fill(255,255,255);
    }
    ellipse(circleXoff,circleYoff,circleSize,circleSize);
    if (pumpon_bool){
      fill(0);
    }
    else{
      fill(255,0,0);
    }
    text("OFF", circleXoff,circleYoff+5);
    //if (pumpon_bool && !automatic_on){
    //  client.publish(publish_topic,PUMPON,QOS,RETAINED);
    //}
}
  public void newmessage(String s) {
    current = s;
    t=Integer.parseInt(current);
    if (t<=soil_dry){
      client.publish(publish_topic,PUMPON,QOS,RETAINED);
      pumpon_bool= true;
      automatic_on=true;
    } else if (t>=soil_dry+hysteresis) {
      client.publish(publish_topic,PUMPOFF,QOS,RETAINED); 
      pumpon_bool=false;
      automatic_on=false;
    } else if (pumpon_bool && t<soil_dry+hysteresis){
      client.publish(publish_topic,PUMPON,QOS,RETAINED);
      pumpon_bool= true;
      automatic_on=true;
    } else if (!pumpon_bool && t<soil_dry+hysteresis && t>soil_dry){
      client.publish(publish_topic,PUMPOFF,QOS,RETAINED); 
      pumpon_bool=false;
      automatic_on=false;
    }
    t=int(map(t,0,1023,height_diagramm,0));
    t=t+diagramm_y_start;
    Points P = new Points(diagramm_x_start+width_diagramm, t );
    poop.add(P);
  
  }




void messageReceived(String topic, byte[] payload) {
  newmessage(new String(payload));
  println("new message: " + topic + " - " + new String(payload));
}

void clientConnected() {
  println("client connected");

  client.subscribe(TOPICS);
}

class Points {
  float x, y;
  Points(float x, float y) {
    this.x = x;
    this.y = y;
  }
}

void update() {
  if ( overCircleon(circleXon, circleYon, circleSize) ) {
    circleonOver = true;
    circleoffOver = false;
  } else if ( overCircleoff(circleXoff, circleYoff, circleSize) ) {
    circleoffOver = true;
    circleonOver = false;
  } else {
    circleoffOver = false;
    circleonOver = false;
  }
  
}

boolean overCircleon(int x, int y, int diameter) {
  float disX = x - mouseX;
  float disY = y - mouseY;
  if (sqrt(sq(disX) + sq(disY)) < diameter/2 ) {
    return true;
  } else {
    return false;
  }
}


boolean overCircleoff(int x, int y, int diameter) {
  float disX = x - mouseX;
  float disY = y - mouseY;
  if (sqrt(sq(disX) + sq(disY)) < diameter/2 ) {
    return true;
  } else {
    return false;
  }
}

void mousePressed() {
  if (circleonOver) {
    pumpon_bool = true;
    if(!automatic_on){
      client.publish(publish_topic,PUMPON,QOS,RETAINED);
    }
  }
  if (circleoffOver) {
    pumpon_bool = false;
    client.publish(publish_topic,PUMPOFF,QOS,RETAINED);
  }
}
