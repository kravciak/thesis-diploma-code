require 'sinatra'
require 'faraday'	# rest client
require 'gyoku'		# params to xml
require 'json'		# parse response data
# require 'active_support/core_ext/hash'	# params to xml

API_URI  = 'http://127.0.0.1:8080/'
GET_URI  = 'statements/87/results'
SEND_URI = 'esper/events'

con = Faraday.new(:url => API_URI)

get '/' do
	res = con.get GET_URI
	@customers = JSON.parse(res.body, symbolize_names: true)
	erb :index
end

post '/upload' do
	# logger.info params.to_xml(:root => 'Contact')
	# logger.info Gyoku.xml('Contact' => params)
	res = con.post SEND_URI do |req|
		req.headers['Content-Type'] = 'application/xml'
		req.body = Gyoku.xml('Contact' => params)
	end
	redirect back
end

post '/delete' do
	con.delete "#{GET_URI}/#{params[:uuid]}"
	redirect back
end