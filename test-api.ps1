# 测试生产者-消费者API
$url = "http://localhost:8080/api/producer-consumer/init"
$headers = @{"Content-Type"="application/json"}
$body = '{"bufferSize":5,"producerCount":2,"consumerCount":2,"simulationSpeed":1000}'

Write-Host "发送请求到: $url"
Write-Host "请求体: $body"

try {
    $response = Invoke-WebRequest -Uri $url -Method Post -Headers $headers -Body $body
    Write-Host "请求成功！状态码: $($response.StatusCode)"
    Write-Host "响应内容: $($response.Content)"
} catch {
    Write-Host "请求失败！错误信息: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $statusDescription = $_.Exception.Response.StatusDescription
        Write-Host "HTTP状态码: $statusCode - $statusDescription"
    }
}