API_URI = 'http://127.0.0.1:8080/'

Her::API.setup url: API_URI do |c|
  # Request
  c.use Faraday::Request::UrlEncoded

  # Response
  c.use Her::Middleware::DefaultParseJSON

  # Adapter
  c.use Faraday::Adapter::NetHttp

  # Fiddler
  # c.proxy "http://127.0.0.1:8888"
end