import { combineViewAndLayout } from 'src/utils';

describe('utils specs', () => {
  describe('combineLayout', () => {
    const view = {
      title: 'Board1',
      status: 'SUCCESS',
      message: 'msg',
      boxes: [{
        title: 'Box1',
        status: 'SUCCESS',
        message: 'msg1',
        checks: [{
          title: 'Check1',
          status: 'SUCCESS',
          message: 'msg11',
          label: 'l11'
        }]
      }, {
        title: 'Box2',
        status: 'WARNING',
        message: 'msg2',
        checks: [{
          title: 'Check2',
          status: 'WARNING',
          message: 'msg22',
          label: 'l22'
        }]
      }]
    };

    const links = [
      ['Box1', 'Box2']
    ];

    const layout = {
      columns: [
        {
          percentage: 50,
          rows: [{
            title: 'Zone 1',
            percentage: 100,
            boxes: [{
              title: 'Box1',
              description: 'desc1',
              labelLimit: 2
            }]
          }]
        },
        {
          percentage: 50,
          rows: [{
            title: 'Zone 2',
            percentage: 100,
            boxes: [{
              title: 'Box2',
              description: 'desc2',
              labelLimit: 0
            }]
          }]
        }
      ]
    };

    it('combines board layout and view data, returns an object representing the view', () => {
      const result = combineViewAndLayout(view, layout, links);
      expect(result).to.deep.equal({
        title: 'Board1',
        status: 'SUCCESS',
        message: 'msg',
        columns: [
          {
            percentage: 50,
            rows: [{
              title: 'Zone 1',
              percentage: 100,
              boxes: [{
                title: 'Box1',
                description: 'desc1',
                status: 'SUCCESS',
                message: 'msg1',
                labelLimit: 2,
                checks: [{
                  title: 'Check1',
                  status: 'SUCCESS',
                  message: 'msg11',
                  label: 'l11'
                }]
              }]
            }]
          },
          {
            percentage: 50,
            rows: [{
              title: 'Zone 2',
              percentage: 100,
              boxes: [{
                title: 'Box2',
                status: 'WARNING',
                description: 'desc2',
                message: 'msg2',
                labelLimit: 0,
                checks: [{
                  title: 'Check2',
                  status: 'WARNING',
                  message: 'msg22',
                  label: 'l22'
                }]
              }]
            }]
          }
        ],
        links
      });
    });
  });
});