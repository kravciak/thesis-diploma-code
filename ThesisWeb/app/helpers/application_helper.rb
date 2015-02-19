module ApplicationHelper

    def flash_class level
      case level
        when :success then "alert-success"
        when :error then "alert-danger"
        when :alert then "alert-warning"
        when :notice then "alert-info"
        else level.to_s
      end
    end
 
    def uuid_to_time(time_id)
	    (UUIDTools::UUID.parse time_id).timestamp
    end

    def uuid_to_str(time_id)
        time = uuid_to_time(time_id)
        format = time.today? ? '%T' : '%F %T'
        time.strftime(format)
    end

    def time_to_expire(time_id, ttl)
    	remaining = ttl - (Time.now - uuid_to_time(time_id)).to_i
        if (remaining < 0)
            '-'
        else
		  ChronicDuration.output(remaining, format: :short)
        end
    end
 
    def statement_uri(statement)
        "#{API_URI}statements/#{statement.id}/results"
    end

    def stream_uri()
        "#{API_URI}esper/events"
    end
end