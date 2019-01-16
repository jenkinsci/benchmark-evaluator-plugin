# Benchmark Evaluator Plugin
This Plugin provides to load benchmark data to Jenkins.<br>
The plugin persists the data and decides on success or failure of the evaluated build. Therefor, Service Level Agreements, software specifications, or relative thresholds can be set in a simple graphical interface.<br>
The Visual Interface visualizes the benchmarking results for human users. It can plot individual runs or the development of aggregates across versions, e.g., as line charts.<br>
Currently the plugin only reads CSV and YCSB data. Using small shell scripts the benchmark results of different tools can be converted into csv files. Classes for new data types can be easily integrated via an interface.<br>
<br>
<br>
# How to use this Plugin<br>
1.) Add a "Benchmark" build step. Specify the file path to the benchmark results.<br><br>
![alt text](https://github.com/Lehmann-Fabian/benchmark-evaluator/blob/master/readme_images/build_step.JPG "Build Step")<br><br><br>
2.) "Benchmark Results" and "Benchmark Configuration" will be added to the project overview.<br><br>
![alt text](https://github.com/Lehmann-Fabian/benchmark-evaluator/blob/master/readme_images/project_overview.JPG "Project Overview")<br><br><br>
3.) Specify the thresholds after the first run or by using the add button.<br><br>
![alt text](https://github.com/Lehmann-Fabian/benchmark-evaluator/blob/master/readme_images/config.JPG "Configuration")<br><br><br>
4) There is a detailed overview for each build.<br><br>
![alt text](https://github.com/Lehmann-Fabian/benchmark-evaluator/blob/master/readme_images/detail_page.JPG "Detail Page")<br><br><br>
5) The results are visualized for all builds in line diagrams.<br><br>
![alt text](https://github.com/Lehmann-Fabian/benchmark-evaluator/blob/master/readme_images/line_graph.JPG "Line Graphs")<br><br><br>
How the plugin can be used can be seen in our paper [tba](https://github.com/Lehmann-Fabian/benchmark-evaluator/).
