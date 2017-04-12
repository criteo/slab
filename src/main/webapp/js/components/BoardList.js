// @flow
import { Component } from 'react';
import { connect } from 'react-redux';
import type { State, BoardConfig } from '../state';
import { fetchBoards } from '../actions';

type Props = {
  boards: Array<BoardConfig>,
  fetchBoards: () => void
};

class BoardList extends Component {
  props: Props;

  componentWillMount() {
    this.props.fetchBoards();
  }

  render() {
    const { boards } = this.props;
    return (
      <div className="board-list">
        {
          boards.map(board =>
            <a href={`/${board.title}`} key={board.title} className="link">{board.title}</a>
          )
        }
      </div>
    );
  }
}

const select = (state: State) => ({
  boards: state.boards
});

const actions = dispatch => ({
  fetchBoards: () => dispatch(fetchBoards())
});

export default connect(select, actions)(BoardList);