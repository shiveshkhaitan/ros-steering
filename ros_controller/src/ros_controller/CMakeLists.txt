cmake_minimum_required(VERSION 2.8.3)
project(ros_controller)

find_package(catkin REQUIRED roscpp geometry_msgs nav_msgs)

catkin_package()

include_directories(
${catkin_INCLUDE_DIRS}
)

add_executable(server src/server_node.cpp)
add_executable(client src/client_node.cpp)

add_dependencies(server ${${PROJECT_NAME}_EXPORTED_TARGETS} ${catkin_EXPORTED_TARGETS})
add_dependencies(client ${${PROJECT_NAME}_EXPORTED_TARGETS} ${catkin_EXPORTED_TARGETS})

target_link_libraries(server
${catkin_LIBRARIES}
)
target_link_libraries(client
${catkin_LIBRARIES}
)