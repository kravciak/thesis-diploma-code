json.array!(@schemas) do |schema|
  json.extract! schema, :id, :root, :xsd
  json.url schema_url(schema, format: :json)
end
