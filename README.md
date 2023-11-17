# Benchmark Evaluator Plugin
This Plugin provides to load benchmark data to Jenkins.

The plugin persists the data and decides on success or failure of the evaluated build.
Therefore, Service Level Agreements, software specifications, or relative thresholds can be set in a simple graphical interface.

The Visual Interface visualizes the benchmarking results for human users.
It can plot individual runs or the development of aggregates across versions, e.g., as line charts.

Currently the plugin only reads [comma-separated values](https://en.wikipedia.org/wiki/Comma-separated_values) (CSV) and [Yahoo Cloud Serving Benchmark](https://en.wikipedia.org/wiki/YCSB) (YCSB) data.
Using small shell scripts the benchmark results of different tools can be converted into CSV files.
Classes for new data types can be easily integrated via an interface.

If you use this software in a publication, please cite it as:

### Text
Martin Grambow, Fabian Lehmann, David Bermbach.
**Continuous Benchmarking: Using System Benchmarking in Build Pipelines**.
In: Proceedings of the 1st Workshop on Service Quality and Quantitative Evaluation in new Emerging Technologies (SQUEET 2019).
IEEE 2019.

### BibTeX
```bibtex
@inproceedings{grambow_continuous_benchmarking:_2019,
    title = {Continuous Benchmarking: Using System Benchmarking in Build Pipelines},
    booktitle = {Proceedings of the 1st Workshop on Service Quality and Quantitative Evaluation in new Emerging Technologies (SQUEET 2019)},
    author = {Grambow, Martin and Lehmann, Fabian and Bermbach, David},
    year = {2019},
    publisher = {IEEE}
}
```

A full list of our [publications](https://www.mcc.tu-berlin.de/menue/forschung/publikationen/parameter/en/) and [prototypes](https://www.mcc.tu-berlin.de/menue/forschung/prototypes/parameter/en/) is available on our group website.

## License
The code in this repository is licensed under the terms of the [MIT](./LICENSE) license.

# How to use this Plugin
1.  Add a "Benchmark" build step.
    Specify the file path to the benchmark results.
    ![alt text](https://github.com/jenkinsci/benchmark-evaluator-plugin/blob/master/readme_images/build_step.JPG "Build Step")

    The files can be located locally or on the Internet and can be available via http/https, as well as on an ftp server.
    Also in the second case it is important that the URL ends with the type (e.g. http://www.example.com/benchmark_results.csv).
    The file type must always be specified locally and on ftp as well.
    A CSV-file must be structured according to the following schema:

    ```
    metric name;value
    ExampleMetric1;40.1
    ExampleMetric2;-42.4
    ```
    - first line is optional and may be a header, which is ignored
    - all following lines must have exactly two columns separated by one semicolon `;` or comma `,`.
    - the first column specifies the name of the metric.
    - the second column specifies the value of the metric. Floating point values should use the dot `.`; a comma `,` is only allowed when semicolon `;` is used as the field separator.

    Optinal a line can be "name;[Build specific name]"
3.  "Benchmark Results" and "Benchmark Configuration" will be added to the project overview.
    ![alt text](https://github.com/jenkinsci/benchmark-evaluator-plugin/blob/master/readme_images/project_overview.JPG "Project Overview")
4.  Specify the thresholds after the first run or by using the add button.
    ![alt text](https://github.com/jenkinsci/benchmark-evaluator-plugin/blob/master/readme_images/config.JPG "Configuration")
5.  There is a detailed overview for each build.
    ![alt text](https://github.com/jenkinsci/benchmark-evaluator-plugin/blob/master/readme_images/detail_page.JPG "Detail Page")
6.  The results are visualized for all builds in line diagrams.
    ![alt text](https://github.com/jenkinsci/benchmark-evaluator-plugin/blob/master/readme_images/line_graph.JPG "Line Graphs")

How the plugin can be used can be seen in our paper [Continuous Benchmarking: Using System Benchmarking in Build Pipelines](http://dbermbach.github.io/publications/2019-squeet.pdf).
