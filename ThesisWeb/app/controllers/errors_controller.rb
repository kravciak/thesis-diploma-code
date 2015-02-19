class ErrorsController < ApplicationController

  def offline
  	begin
  		Esper.ping
  		redirect_to root_path
  	rescue
  		flash.now[:error] = "Esper engine is offline."
  	end
  end
  
end
