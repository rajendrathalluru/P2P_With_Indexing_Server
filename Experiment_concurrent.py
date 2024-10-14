import matplotlib.pyplot as plt

# Sample data for average response times
clients = [10, 20, 50, 80, 100]  # Number of concurrent clients
response_times = [5.7, 15.35, 10.48, 26.69,64.83]  # Average response times (ms)

# Plot
plt.plot(clients, response_times, marker='o')
plt.title('Average Response Time vs Number of Concurrent Clients')
plt.xlabel('Number of Concurrent Clients')
plt.ylabel('Average Response Time (ms)')
plt.grid(True)
plt.show()
