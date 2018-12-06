#include <thread>

#include <ros/ros.h>
#include <nav_msgs/Odometry.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#define PORT 5679

using namespace std;

double x = 0.0, z = 0.0;
const char* send_msg = "N/A N/A\n";
char* default_msg = "N/A N/A\n";
double update = 0.0;

void send_info(){
    int server_fd, new_socket, valread;
    struct sockaddr_in address;
    int opt = 1;
    int addrlen = sizeof(address);
    if((server_fd = socket(AF_INET, SOCK_STREAM, 0))==0)
    {
        perror("socket failed");
        exit(EXIT_FAILURE); 
    }
    if(setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt)))
    {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(PORT); 
    if(bind(server_fd, (struct sockaddr *)&address, sizeof(address))<0)
    {
        perror("bind failed");
        exit(EXIT_FAILURE);  
    }
    if(listen(server_fd,3) < 0)
    {
        perror("listen");
        exit(EXIT_FAILURE);
    }
    while(ros::ok()){
    if((new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t*)&addrlen))<0)
    {
        perror("accept");
        exit(EXIT_FAILURE);
    }
        char buffer[1024] = {0};
    read(new_socket, buffer, 1024);
        if(!strcmp(buffer,"Request Odom") && ((ros::Time::now().toSec() - update) < 2.0))
            send(new_socket, send_msg, strlen(send_msg), 0);
        else
            send(new_socket, default_msg, strlen(default_msg), 0);
        close(new_socket);  
    }
}

void callback(nav_msgs::Odometry msg){
    update = ros::Time::now().toSec();
    x = round(msg.twist.twist.linear.x * 100)/100;
    z = round(msg.twist.twist.angular.z * 100)/100;
    stringstream ss;
    ss<<fixed<<setprecision(1)<<x<<" "<<z<<"\n";
    string temp = ss.str();
    send_msg = temp.c_str();
}

int main(int argc, char** argv){
    ros::init(argc, argv, "Client");
    ros::NodeHandle nh;
    ros::Subscriber sub = nh.subscribe("/odom", 1, callback);
    thread sender(send_info);
    sender.detach();
    ros::spin();
}
