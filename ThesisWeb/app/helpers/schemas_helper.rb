module SchemasHelper

    def format_xml(xml_string)
        doc = Nokogiri.XML(xml_string) do |config|
            config.noblanks
        end
        doc.to_xml(:indent => 4)
    end
end
