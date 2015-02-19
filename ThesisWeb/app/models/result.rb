class Result
	include Her::Model
	belongs_to :statement
	parse_root_in_json true, format: :active_model_serializers
	
	# attributes :time_id, :statement_id, :event
	
	primary_key :time_id

	def self.destroy_one(statement_id, result_id)
		delete_raw("statements/#{statement_id}/results/#{result_id}")
	end

	def self.destroy_all(statement_id)
		delete_raw("statements/#{statement_id}/results")
	end

	def self.replay_range(statement_id, as, time_start, time_stop)
		get_raw URI.encode("statements/#{statement_id}/results/replay?as=#{as}&start=#{time_start}&stop=#{time_stop}") do |parsed_data, response|
			parsed_data[:data]
		end
	end

	def self.replay_one(statement_id, result_id, as)
		get_raw URI.encode("statements/#{statement_id}/results/#{result_id}/replay?as=#{as}") do |parsed_data, response|
			parsed_data[:data]
		end
	end

	def self.count(statement_id, time_start, time_stop)
		Faraday.get("#{API_URI}statements/#{statement_id}/results/count?start=#{time_start}&stop=#{time_stop}").body
	end

end