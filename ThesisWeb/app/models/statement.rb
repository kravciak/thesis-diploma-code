class Statement
	include Her::Model
	has_many :results
	parse_root_in_json true, format: :active_model_serializers

	attributes :id, :name, :epl, :state, :ttl

	def self.control(id, cmd)
		get_raw("statements/#{id}/control?cmd=#{cmd}")
	end

	validates :name, presence: true
	validates :name, length: { maximum: 100 }

	validates :epl, presence: true
	validates :epl, length: { maximum: 10000 }

end
