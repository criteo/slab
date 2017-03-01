// @flow
import { Component } from 'react';

import type { Box, Check } from '../state';
import BoxModal from './BoxModal';

type Props = {
  box: Box
};

type State = {
  isModalOpen: boolean
};

class BoxView extends Component {
  props: Props;
  state: State;
  constructor(props: Props) {
    super(props);
    this.state = {
      isModalOpen: false
    };
  }

  render() {
    const { box } = this.props;
    const labelLimit = box.labelLimit > -1 ? box.labelLimit : box.checks.length;
    return (
      <div className={`box ${box.status}`} onClick={this.handleBoxClick}>
        <h3>{ box.title }</h3>
        { box.message ? <strong>{ box.message }</strong> : null }
        <div className="checks">
          {
            box.checks.slice(0, labelLimit).map((c: Check) =>
              <span className={`check ${c.status}`} key={c.title}>{c.label || c.title}</span>
            )
          }
        </div>
        {
          labelLimit !== 0 && labelLimit < box.checks.length &&
          <div id="more">...</div>
        }
        <BoxModal
          isOpen={this.state.isModalOpen}
          box={box}
          onCloseClick={ () => this.setState({ isModalOpen: false }) }
        />
      </div>
    );
  }

  handleBoxClick = () => {
    this.setState({ isModalOpen: true });
  }
}

export default BoxView;
