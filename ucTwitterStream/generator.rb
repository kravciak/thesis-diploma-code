require 'twitter'
require 'builder'

API_URI = 'http://127.0.0.1:8080/'

trap "SIGINT" do
  puts "Exiting."
  exit 130
end

# Create schema
puts "Uploading schema."
conn = Faraday.new(API_URI)
conn.post '/schemas', {name: 'TweetEvent', root: 'TweetEvent', xsd: File.read('schema.xsd')}

# Configure twitter
client = Twitter::Streaming::Client.new do |config|
  config.consumer_key        = "mampm0Yhxqj6QXymcRfUTnPVB"
  config.consumer_secret     = "gV486UTibCD6kC5qH7KTRYPvkY5y5mjVB8OcoSsUkCw3SK2Ix1"
  config.access_token        = "307580546-rre2kr15r7BQEMgLWfaptVRKOcnjqlZ7cNJ63uEC"
  config.access_token_secret = "wkONOZXTdRomnkYka9xNz0Lt24hlaDwrRuHfugHs2qWqN"
end

# Send events
puts "Generating."
client.sample do |object|
	# puts "[#{object.user.screen_name}] #{object.text}" if object.is_a?(Twitter::Tweet)
	if object.is_a?(Twitter::Tweet)
		builder = Builder::XmlMarkup.new
  		xml = builder.TweetEvent { |b| b.username(object.user.screen_name); b.message(object.text) }

		conn.post do |req|
			req.url '/esper/events'
			req.headers['Content-Type'] = 'application/xml'
			req.body = xml
		end
  end
end
