crumb :root do
  link "Home", root_path
end

crumb :schemas do
  link "Schemas", schemas_path
end

crumb :schema do |schema|
  parent :schemas
  link truncate(schema.name, length:25), schema
end

crumb :new_schema do
  parent :schemas
  link "New Schema", new_schema_path
end

crumb :statements do
  link "Statements", statements_path
end

crumb :statement do |statement|
  parent :statements
  link truncate(statement.name, length:25), statement
end

crumb :new_statement do
  parent :statements
  link "New Statement", new_statement_path
end

crumb :stream do
  link "Stream", stream_index_path
end

crumb :results do |statement|
  parent :statement, statement
  link "Results"
end

crumb :result do |result|
  parent :statement, result.statement
  link result.id
end

# crumb :projects do
#   link "Projects", projects_path
# end

# crumb :project do |project|
#   link project.name, project_path(project)
#   parent :projects
# end

# crumb :project_issues do |project|
#   link "Issues", project_issues_path(project)
#   parent :project, project
# end

# crumb :issue do |issue|
#   link issue.title, issue_path(issue)
#   parent :project_issues, issue.project
# end

# If you want to split your breadcrumbs configuration over multiple files, you
# can create a folder named `config/breadcrumbs` and put your configuration
# files there. All *.rb files (e.g. `frontend.rb` or `products.rb`) in that
# folder are loaded and reloaded automatically when you change them, just like
# this file (`config/breadcrumbs.rb`).