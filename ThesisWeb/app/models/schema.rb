class Schema
	include Her::Model
	parse_root_in_json true, format: :active_model_serializers
	
	attributes :id, :name, :root, :xsd

	validates :name, presence: true
	validates :name, length: { maximum: 100 }

	validates :root, presence: true
	validates :root, length: { maximum: 100 }

	validates :xsd, presence: true
end
