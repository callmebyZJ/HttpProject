# HttpProject
HttpServer, HttpClient, QPS

------

目录结构：

> HttpClient中包含串行10000字的结果，其logs文件中含有不同QPS值的压测结果以及可用性>99.95%的最大QPS

├─HttpClient
│  │  result_totalCount_10000.txt
│  ├─logs
│  │      result_10.log
│  │      ......
│  │      result_550.log
│  │      result_MAX_QPS.log 
│  ├─src    
└─HttpServer
    ├─src