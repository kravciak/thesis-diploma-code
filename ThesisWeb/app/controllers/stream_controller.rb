class StreamController < ApplicationController

	def index
	end

	def upload
		conn = Faraday.new("#{API_URI}esper/events")

		data = params[:input_xml]
		if !data.blank?
			res = conn.post do |req|
				req.headers['Content-Type'] = 'application/xml'
				req.body = data
			end
			if res.status != 200
				flash[:error] = res.body
				return redirect_to :back
			end
		end

		file = params[:file_xml]
		if !file.blank?
			res = conn.post do |req|
				req.headers['Content-Type'] = 'application/xml'
				req.headers['Content-Length'] = file.size.to_s
				req.body = Faraday::UploadIO.new(file, 'application/xml')
			end
			if res.status != 200
				flash[:error] = res.body
				return redirect_to :back
			end
		end

		redirect_to :back, notice: 'New Events Processed.'
	end

	def sample
		conn = Faraday.new("#{API_URI}esper/events")

		files=["sim_tweets.xml", "sim_stocks.xml"]
		files.each do |filename|
			file=Rails.root.join "public", "events", filename
			conn.post do |req|
				req.headers['Content-Type'] = 'application/xml'
				req.headers['Content-Length'] = file.size.to_s
				req.body = Faraday::UploadIO.new(file, 'application/xml')
			end
		end

		redirect_to :back, notice: 'Event genarators simulation data sent.'
	end
  
private

	def api_params
		params.permit(:input_xml, :file_xml)
	end

end

=begin
  	file=params[:file_xml]
	uri = URI("#{API_URI}/esper/events")

	req = Net::HTTP::Post.new(uri.path)
	req.content_type = 'application/xml'
	req.content_length = file.size
	req.body_stream = file.open

	res = Net::HTTP.start(uri.host, uri.port) {|http|
    	http.request(req)
  	}
=end