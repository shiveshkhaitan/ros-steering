#include <thread>

#include <ros/ros.h>
#include <geometry_msgs/Twist.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#define PORT 5678

using namespace std;

void receive(ros::Publisher pub){
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
    geometry_msgs::Twist twist;
    while(true){
    if((new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t*)&addrlen))<0)
    {
        perror("accept");
        exit(EXIT_FAILURE);
    }
        char buffer[1024] = {0};
        read(new_socket, buffer, 1024);
        string linear, angular;
        int count = 0;
        for(int i = 0; i<strlen(buffer); i++){
            if(buffer[i]==' '){
                count++;
                continue;
            }
            if(!count)
                linear+=buffer[i];
            else
                angular+=buffer[i];
        }
        stringstream ss(linear);
        ss>>twist.linear.x;
        stringstream ss2(angular);
        ss2>>twist.angular.z;
        pub.publish(twist);
    }
}


int main(int argc, char** argv){
    ros::init(argc, argv, "Server");
    ros::NodeHandle nh;
    ros::Publisher pub = nh.advertise<geometry_msgs::Twist>("/cmd_vel", 10);
    thread receiver(receive, pub);
    receiver.detach();
    ros::spin();
}
