var BookmarkDetail = React.createClass({
  loadBookmarks: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  getInitialState: function() {
    return {data: []};
  },
  componentDidMount: function() {
    this.loadBookmarks();
  },
  handleSubmit: function(e) {
        e.preventDefault();

        var formData = $("#bookmarkForm").serialize();

        var saveUrl = "/bookmarks/";
        $.ajax({
            url: saveUrl,
            method: 'POST',
            dataType: 'json',
            data: formData,
            cache: false,
            success: function(data) {
                var tempBookmarks = this.state.data.slice();
                tempBookmarks.push(data);
                this.setState({ data: tempBookmarks});
                $("#bookmarkForm")[0].reset();
            }.bind(this),
            error: function(xhr, status, err) {
                $("#bookmarkForm")[0].reset();
                alert(err.toString());
            }.bind(this)
        });

        return;
  },
  delete: function(e) {
        e.preventDefault();

        var formData = $("#bookmarkForm").serialize();
        var idToDelete = e.target.dataset.id;

        var deleteUrl = "/bookmarks/" + idToDelete;
        $.ajax({
            url: deleteUrl,
            method: 'DELETE',
            dataType: 'json',
            data: formData,
            cache: false,
            success: function(data) {
                var tempBookmarks = this.state.data.slice();
                var removeIndex = tempBookmarks.findIndex(function(e){return e.id==idToDelete})
                tempBookmarks.splice(removeIndex, 1);
                this.setState({ data: tempBookmarks});
                $("#bookmarkForm")[0].reset();
            }.bind(this),
            error: function(xhr, status, err) {
                $("#bookmarkForm")[0].reset();
                alert(err.toString());
            }.bind(this)
        });

        return;
  },
  render: function() {
    return (
      <div className="">
        <div style={{display: 'flow-root'}}>
          <h1 style={{float:'left'}}>URL Shortener</h1>
          <a href="/logout" style={{float:'right'}}>
            <button>logout</button>
          </a>
          <a style={{float:'right', 'padding-right':'20px'}}>
            <button onClick={function(){$.get('/me').success(function(data){alert(data.key)})}} >show my key</button>
          </a>
        </div>
        <div>
          <BookmarkForm handleSubmit={this.handleSubmit} />
          <Bookmarks delete={this.delete} data={this.state.data} />
        </div>
      </div>
    );
  }
});

var Bookmarks = React.createClass({
  render: function() {
      var self = this;
      var bookmarkNodes = this.props.data.map(function (bookmark) {
          return (
              <Bookmark key={bookmark.id} id={bookmark.id} name={bookmark.name} url={bookmark.url} slug={bookmark.slug} delete={self.props.delete}/>
          );
      });
      return (
          bookmarkNodes.length > 0 &&
          <div className="well">
            <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>URL</th>
                    <th>Slug</th>
                  </tr>
                </thead>
              <tbody>
                {bookmarkNodes}
              </tbody>
            </table>
          </div>
      );
  }
});

var Bookmark = React.createClass({
  render: function() {
    return (
        <tr>
          <td>{this.props.name}</td>
          <td><a href={this.props.url}>{this.props.url}</a></td>
          <td><a href={"/"+this.props.slug}>{this.props.slug}</a></td>
          <td data-id={this.props.id} onClick={this.props.delete} style={{'cursor':'pointer'}}>x</td>
        </tr>
    );
  }
});

var BookmarkForm = React.createClass({
  render: function() {
    return (
    	<div className="row">
      		<form id="bookmarkForm" onSubmit={this.props.handleSubmit}>
		        <div className="col-xs-3">
		          <div className="form-group">
		            <input type="text" name="name" required="required" ref="name" placeholder="Name" className="form-control" />
		          </div>
		        </div>
		        <div className="col-xs-6">
		          <div className="form-group">
		            <input type="url" name="url"required="required"  ref="url" placeholder="URL" className="form-control" />
		          </div>
		        </div>
		        <div className="col-xs-3">
		          <input type="submit" className="btn btn-block btn-info" value="Add" />
		        </div>
			</form>
	   </div>
    );
  }
});

React.render(<BookmarkDetail url="/bookmarks/" />, document.getElementById('content'));
