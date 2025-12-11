import redis
import time
import statistics

def test_redis_latency(host, port=6379, iterations=100):
    r = redis.Redis(host=host, port=port, socket_connect_timeout=5)

    latencies = []
    successful = 0

    for i in range(iterations):
        try:
            start = time.time_ns()
            response = r.ping()
            end = time.time_ns()

            if response:
                latency_ms = (end - start) / 1_000_000  # 转换为毫秒
                latencies.append(latency_ms)
                successful += 1

            # 避免连续请求太快
            time.sleep(0.01)

        except Exception as e:
            print(f"请求失败: {e}")
            continue

    if latencies:
        print(f"测试完成: {successful}/{iterations} 成功")
        print(f"平均延迟: {statistics.mean(latencies):.2f}ms")
        print(f"中位数延迟: {statistics.median(latencies):.2f}ms")
        print(f"标准差: {statistics.stdev(latencies):.2f}ms")
        print(f"最小延迟: {min(latencies):.2f}ms")
        print(f"最大延迟: {max(latencies):.2f}ms")

        # 百分位延迟
        sorted_latencies = sorted(latencies)
        for p in [50, 90, 95, 99]:
            percentile = sorted_latencies[int(len(sorted_latencies) * p / 100)]
            print(f"P{p}延迟: {percentile:.2f}ms")

    return latencies

# 使用
latencies = test_redis_latency('106.15.90.163', iterations=200)