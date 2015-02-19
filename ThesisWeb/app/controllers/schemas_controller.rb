class SchemasController < ApplicationController
  before_action :set_schema, only: [:show, :edit, :update, :destroy, :export]

  def export
    send_data @schema.xsd, type:'text/xml', filename: @schema.name+'.xsd'
  end

  def index
    limit = 10
    page = params[:page] || 1
    offset = (page.to_i - 1) * limit
      
    @schemas = Schema.all(offset:offset, limit:limit, filter:params[:filter])
    @pagination = @schemas.paginate(page:params[:page], per_page:limit, total_entries: @schemas.metadata[:total_count])
  end

  def show
  end

  def new
    @schema = Schema.new
  end

  def edit
  end

  def upload
    
  end

  def create
    @schema = Schema.new(schema_params)
    
    if @schema.valid?
      if @schema.save
        redirect_to @schema, notice: 'Schema was successfully created.'
      else
        set_schemas
        flash.now[:error] = @schema.message
        render action: 'new'
      end
    else
        render action: 'new'
    end
  end

  def update
    # Schema.save_existing(5, schema_params)
    # render action: 'edit'
    # @schema.assign_attributes(schema_params)
    
=begin
    @schema.root="newroot"
    if @schema.save
      redirect_to @schema, notice: 'Schema was successfully updated.'
    else
      flash.now[:error] = "Unable to save schema."
      render action: 'edit'
    end
=end
  end

  def destroy
    @schema.destroy
    redirect_to schemas_path
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_schema
      @schema = Schema.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list usagegh.
    def schema_params
      params.require(:schema).permit(:name, :root, :xsd)
    end
end
