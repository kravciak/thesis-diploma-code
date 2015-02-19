class Esper
	include Her::Model
	collection_path "esper"

	custom_get :ping
end