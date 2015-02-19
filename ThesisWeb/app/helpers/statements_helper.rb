module StatementsHelper

    def sec_to_str(seconds)
    	if seconds == 0
    		'&infin;'.html_safe
    	else
        	ChronicDuration.output(seconds, format: :long)
        end
    end

end
