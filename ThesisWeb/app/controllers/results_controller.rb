class ResultsController < ApplicationController

	def index
		if !params[:statement_id].eql?('nil')
			@statement = Statement.find(params[:statement_id])
			@results = @statement.results.all(
		      	offset: params[:offset] || 'first',
		      	reverse: params[:reverse] || false,
		      	limit: 10)
		else
			flash[:error] = 'You need to select statement first.'
			redirect_to statements_path
		end
	end

	def show
    	@statement = Statement.find(params[:statement_id])
		@result = @statement.results.find(params[:id])
		if @result.id.nil?
			flash[:error] = @result.message
			redirect_to statement_path(@statement)
		end
	end

	def destroy
		Result.destroy_one(params[:statement_id], params[:id])
		redirect_to statement_results_path(params[:statement_id]), notice: 'Result was removed.'
	end

	def clean
		Result.destroy_all(params[:statement_id])
		redirect_to :back, notice: 'Results were cleaned.'
	end

	def export
	    if params[:id].nil?
	    	redirect_to "#{API_URI}statements/#{params[:statement_id]}/results/export"
	    else
	    	statement = Statement.find(params[:statement_id])
	    	result = statement.results.find(params[:id])
	    	if !result.id.nil?
	    		send_data result.attributes.to_json, type:'application/json', filename:'export.json'
			else
				flash[:error] = result.message
				redirect_to statement_path(statement)
	    	end
	    end
	end

	# Count how many results are in range
	def count
		if params[:start].blank? || params[:stop].blank?
			# render :json => { :success => false }
		else
			res = Result.count(params[:statement_id], params[:start], params[:stop])
			render :json => res
		end
	end

	def replay
    	res = Result.replay_one(params[:statement_id], params[:id], params[:as])
    	if res[:message].nil?
    		flash[:notice] = "Event sent to engine for processing."
    	else
	   		flash[:error] = res[:message]
    	end
    	redirect_to :back
	end

end
