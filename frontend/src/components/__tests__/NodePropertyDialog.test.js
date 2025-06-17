import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import NodePropertyDialog from '../NodePropertyDialog.vue'

describe('NodePropertyDialog', () => {
  const mockNode = {
    id: 'node1',
    label: 'Test Node',
    type: 'action',
    deviceType: 'light',
    deviceAction: 'on',
    customProps: [
      { name: 'delay', value: '1000' }
    ]
  }

  let wrapper;

  beforeEach(() => {
    wrapper = mount(NodePropertyDialog, {
      props: {
        visible: true,
        node: mockNode,
      },
    });
  });

  it('renders correctly when visible', () => {
    expect(wrapper.find('.dialog').exists()).toBe(true);
    expect(wrapper.find('h3').text()).toBe('节点属性配置');
    expect(wrapper.find('input[placeholder="节点名称"]').element.value).toBe('Test Node');
    expect(wrapper.find('input[placeholder="设备类型"]').element.value).toBe('light');
    expect(wrapper.find('input[placeholder="设备动作"]').element.value).toBe('on');
    expect(wrapper.findAll('.custom-property-item').length).toBe(1);
    expect(wrapper.find('.custom-property-item input[placeholder="自定义属性名"]').element.value).toBe('delay');
    expect(wrapper.find('.custom-property-item input[placeholder="属性值"]').element.value).toBe('1000');
  });

  it('emits save event with updated node data', async () => {
    await wrapper.find('input[placeholder="节点名称"]').setValue('Updated Node');
    await wrapper.find('input[placeholder="设备类型"]').setValue('fan');
    await wrapper.find('input[placeholder="设备动作"]').setValue('off');

    await wrapper.find('button.save-btn').trigger('click');

    const emitted = wrapper.emitted('save');
    expect(emitted).toBeTruthy();
    expect(emitted[0][0].label).toBe('Updated Node');
    expect(emitted[0][0].deviceType).toBe('fan');
    expect(emitted[0][0].deviceAction).toBe('off');
    expect(emitted[0][0].customProps[0].name).toBe('delay');
    expect(emitted[0][0].customProps[0].value).toBe('1000');
  });

  it('emits cancel event', async () => {
    await wrapper.find('button.cancel-btn').trigger('click');
    expect(wrapper.emitted('cancel')).toBeTruthy();
  });

  it('adds a new custom property', async () => {
    await wrapper.find('.new-property-name').setValue('newProp');
    await wrapper.find('.new-property-value').setValue('newValue');
    await wrapper.find('button.add-btn').trigger('click');
    
    expect(wrapper.findAll('.custom-property-item').length).toBe(2);
    expect(wrapper.emitted('save')).toBeFalsy(); // Should not save automatically

    // Verify the new property is in the localNode for display
    const newPropItem = wrapper.findAll('.custom-property-item')[1];
    expect(newPropItem.find('input[placeholder="自定义属性名"]').element.value).toBe('newProp');
    expect(newPropItem.find('input[placeholder="属性值"]').element.value).toBe('newValue');
  });

  it('removes a custom property', async () => {
    await wrapper.find('button.remove-btn').trigger('click');
    expect(wrapper.findAll('.custom-property-item').length).toBe(0);
    expect(wrapper.emitted('save')).toBeFalsy(); // Should not save automatically
  });

  it('handles node without customProps initially', () => {
    const nodeWithoutCustomProps = {
      id: 'nodeX',
      label: 'Node X',
      type: 'action',
    };
    const wrapperWithoutProps = mount(NodePropertyDialog, {
      props: {
        visible: true,
        node: nodeWithoutCustomProps,
      },
    });
    expect(wrapperWithoutProps.findAll('.custom-property-item').length).toBe(0);
    expect(wrapperWithoutProps.find('.new-custom-property').exists()).toBe(true);
  });
}); 