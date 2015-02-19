class StatementsController < ApplicationController
  before_action :set_statement, only: [:show, :edit, :update, :destroy]

  def control
    Statement.control(params[:id], params[:cmd])
    redirect_to :back
  end

  def replay
    if params[:start].blank? or params[:stop].blank?
      flash[:error] = "Date values are required."
    else
      res = Result.replay_range(params[:id], params[:as], params[:start], params[:stop])
      if res[:message].nil?
        flash[:notice] = "Events sent to engine for processing."
      else
        flash[:error] = res[:message]
      end
    end

    redirect_to :back
  end

  def index
    limit = 10
    page = params[:page] || 1
    offset = (page.to_i - 1) * limit
    
    @statements = Statement.all(offset:offset, limit:limit, filter:params[:filter])
    @pagination = @statements.paginate(page:params[:page], per_page:limit, total_entries: @statements.metadata[:total_count])
  end

  def show
  end

  def new
    @statement = Statement.new
  end

  def edit
  end

  def create
    @statement = Statement.new(statement_params)

    if @statement.valid?
      if @statement.save
        redirect_to @statement, notice: 'Statement was successfully created.'
      else
        flash.now[:error] = @statement.message
        render action: 'new'
      end
    else
        render action: 'new'
    end
  end

  def update
    # esper = Epl.save_existing(@statement.id, epl: @statement.statement)
    if false
      redirect_to @statement, notice: 'Statement was successfully updated.'
    else
      render action: 'edit'
    end
  end

  def destroy
      @statement.destroy
      redirect_to statements_path
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_statement
      @statement = Statement.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def statement_params
      params.require(:statement).permit(:name, :epl, :ttl)
    end

end
