# Database

## Indexing

https://levelup.gitconnected.com/system-design-concepts-database-indexing-essentials-e1127e2ded59

---

## Performance

Below are some of the ways to measure database performance:
1. Query Performance
Measure execution time and use EXPLAIN plans to optimize queries.
2. Response Time
Track round-trip time for queries, aiming for low latency.
3. Throughput
Monitor how many queries the database handles per second/minute.
4. Database Connections
Monitor the number of active connections and optimize connection pooling.
5. Disk I/O
Measure read/write speeds, queue length, and disk throughput.
6. CPU Usage
Track CPU utilization to ensure the database is not overburdened.
7. Memory Usage
Monitor memory consumption to avoid excessive usage leading to slowdowns.
8. Lock Contention
Track lock conflicts and deadlocks to avoid delays.
9. Cache Hit Ratio
Monitor cache hit ratios to ensure frequently accessed data is cached.
10. Network Latency
Measure round-trip time for data transfer between the application and database.
11. Slow Query Logs
Capture and analyze slow queries for optimization.
12. Index Optimization
Ensure efficient indexing and monitor index fragmentation.
13. Query Execution Plan:
Mentioned briefly but should include advice on using query profiling tools like EXPLAIN in SQL databases.
14. Database Schema Design:
Optimizing schema design impacts performance and should be addressed.
By tracking these metrics using monitoring tools (e.g., Prometheus, New Relic, MySQL’s EXPLAIN, Query profiling), you can spot and address performance bottlenecks.

---

## Design scalable DB

Here’s how to approach it:

1. Database Schema Design
- Start with a normalized schema to eliminate redundancy and ensure consistency.
- For performance-critical applications, we can selectively denormalize certain tables to optimize read-heavy operations.

2. Horizontal Scaling
- Implement sharding to distribute data across multiple nodes. For example, we can shard based on user ID or geographic regions to evenly distribute the load.
- Partition large tables logically, such as by time ranges (e.g., monthly partitions for a logging system).

3. Replication
- Use master-slave replication where the master handles writes, and the replicas handle reads, improving both performance and reliability.
- In more complex systems, multi-master replication can be used to handle writes from multiple locations.

4. Caching
- Integrate caching solutions like Redis or Memcached to store frequently accessed data in memory, reducing the load on the database.
- Use query-level caching for expensive operations to further enhance performance.

5. Load Balancing
- Add a load balancer to distribute queries across multiple database instances, ensuring no single node becomes a bottleneck.

6. Asynchronous Processing
- For write-intensive operations, we can leverage message queues like Kafka or RabbitMQ to handle tasks asynchronously, reducing the immediate load on the database.

7. Cloud or Distributed Databases
- For large-scale applications, we can consider databases like Cassandra, CockroachDB, or MongoDB that are inherently distributed and designed for horizontal scaling.
- Alternatively, use managed cloud services like AWS RDS or Google Cloud SQL that offer built-in scaling and fault tolerance.

8. Monitoring and Optimization
- Regularly monitor database metrics like query performance, CPU usage, memory utilization, and disk I/O using tools like Prometheus or Grafana.
- Continuously optimize slow queries and ensure indexes are up to date.

9. Archiving and Data Management
- Archive older, less-used data to a separate storage system to keep the active dataset manageable. This helps maintain fast query performance on current data.

By combining these strategies, the database can handle increased traffic, maintain low latency, and remain resilient as the system grows.

Challenges:
1. Data Consistency: In distributed databases, maintaining strong consistency can be challenging. We can use appropriate consistency models, like eventual consistency where acceptable.
2. Shard Management: Resharding as data grows can be complex. To mitigate this, we can plan sharding keys carefully from the beginning.
3. Query Optimization: Complex joins across shards or replicas can slow down performance. We can design queries and schema to minimize such operations.

Proactively monitoring and iterating on the design would help address these challenges effectively.
